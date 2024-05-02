import json
import logging
import os

import requests
import time

logger = logging.getLogger(__name__)

DOWNLOAD_FILE_CHUNK_SIZE = 8192


class AsyncDownloader:

    token = None

    def __init__(self, key: str, secret: str, domain: str = "https://api.clarity.ai"):
        self.domain = domain
        self.key = key
        self.secret = secret

    def download(self, uri: str, body: dict) -> str:
        job_id = self._request_async(uri, body)
        self._wait_for_job(job_id)
        return self._download_job_result(job_id)

    def _get_token(self) -> str:
        if not self.token:
            self.token = self._request_new_token()
        return self.token

    def _request_new_token(self) -> str:
        logging.info(f"Requesting new token...")
        body = {
            "key": self.key,
            "secret": self.secret
        }

        r = requests.post(self.domain + "/clarity/v1/oauth/token", json=body).json()

        if not self._success_login(r):
            error_message = r
            if isinstance(r, dict):
                if "status" in r:
                    error_message = r['status']
                else:
                    error_message = r['message']
            logger.error(f"Unable to get token: {error_message}")
            raise RuntimeError("Cannot get authentication token for Public API")

        return r['token']

    @staticmethod
    def _success_login(response):
        return isinstance(response, dict) and "token" in response

    def _get_headers(self):
        return {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self._get_token()}"
        }

    def _request_async(self, uri, data) -> str:
        url = f"{self.domain}/clarity/v1/public{uri}"
        logger.info(f"Requesting Job to '{url}' with data {data}")

        headers = self._get_headers()

        r = requests.post(url, headers=headers, data=json.dumps(data)).json()

        if not isinstance(r, dict) or "uuid" not in r:
            error_message = r
            if isinstance(r, dict):
                if "message" in r:
                    error_message = r['message']
                if "elements" in r:
                    error_message = r['elements'][0]['message']
            logger.error(f"Error requesting async: {error_message}")
            raise RuntimeError("Error requesting async job")

        requested_uuid = r['uuid']
        logger.info(f"Requested Job with UUID: {requested_uuid}")
        return requested_uuid

    def _get_status(self, job_id):
        status_url = f"{self.domain}/clarity/v1/public/job/{job_id}/status"
        headers = self._get_headers()
        return requests.get(status_url, headers=headers).json()['statusMessage']

    def _wait_for_job(self, job_id):
        logger.info(f"Waiting for Job {job_id} to finish...")
        status_job_message = self._get_status(job_id)
        while status_job_message == "RUNNING":
            time.sleep(30)
            status_job_message = self._get_status(job_id)

        if status_job_message != "SUCCESS":
            logger.error("Finished with error: " + status_job_message + ". JobId: " + job_id)
            raise RuntimeError(f"Async job {job_id} finished with error: {status_job_message}")
        else:
            logger.info(f"Async Job {job_id} finished successfully")

    def _download_job_result(self, job_id: str) -> str:
        download_url = f"{self.domain}/clarity/v1/public/job/{job_id}/fetch"
        local_filename = f"{job_id}.gz"
        return self._download_file(download_url, local_filename)

    def _download_file(self, download_url: str, local_filename: str) -> str:
        logging.info(f"Downloading file from {download_url} to {local_filename}.")

        headers = self._get_headers()

        r = requests.get(download_url, headers=headers, stream=True)
        if r.status_code != 200:
            logger.error(f"Error getting job result: {r.status_code}")
            raise RuntimeError(f"Error downloading job content: {r.status_code}")

        with open(local_filename, 'wb') as f:
            for chunk in r.iter_content(chunk_size=DOWNLOAD_FILE_CHUNK_SIZE):
                f.write(chunk)

        logging.info(f"Downloaded file from {download_url} to {local_filename}. "
                     f"File size {os.path.getsize(local_filename)}")
        return local_filename
