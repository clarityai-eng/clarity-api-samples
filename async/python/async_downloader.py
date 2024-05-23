import json
import logging
import os
import tempfile

import requests
import time

from http.client import OK
from typing import Any

logger = logging.getLogger(__name__)



class AsyncDownloader:

    DOWNLOAD_FILE_CHUNK_SIZE = 8192

    def __init__(self, key: str, secret: str, domain: str = "https://api.dev.clarity.ai"):
        self.domain = domain
        self.key = key
        self.secret = secret
        self.token = None

    def download(self, uri: str, body: dict) -> str:
        job_id = self._request_async(uri, body)
        self._wait_for_job(job_id)
        return self._download_job_result(job_id)

    def _request_async(self, uri, data) -> str:
        url = f"{self.domain}/clarity/v1/public{uri}"
        logger.info(f"Requesting Job to '{url}' with data {data}")

        headers = self._get_headers()

        response = requests.post(url, headers=headers, data=json.dumps(data)).json()

        if isinstance(response, dict) and "uuid" not in response:
            requested_uuid = response['uuid']
            logger.info(f"Requested Job with UUID: {requested_uuid}")
            return requested_uuid

        if not isinstance(response, dict):
            logger.error(f"Error requesting async: {response}")
            raise RuntimeError("Error requesting async job")

        error_message = None
        if "message" in response:
            error_message = response['message']

        if "elements" in response:
            error_message = response['elements'][0]['message']

        logger.error(f"Error requesting async: {error_message}")
        raise RuntimeError("Error requesting async job")

    def _get_headers(self) -> dict:
        return {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self._get_token()}"
        }

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

        response = requests.post(self.domain + "/clarity/v1/oauth/token", json=body).json()

        if self._success_login(response):
            return response['token']

        error_message = response
        if isinstance(response, dict):
            error_message = response.get('status') or response['message']

        logger.error(f"Unable to get token: {error_message}")
        raise RuntimeError("Cannot get authentication token for Public API")

    @staticmethod
    def _success_login(response: Any) -> bool:
        return isinstance(response, dict) and "token" in response

    def _wait_for_job(self, job_id: str) -> None:
        logger.info(f"Waiting for Job {job_id} to finish...")
        status_job_message = self._get_status(job_id)

        while status_job_message == "RUNNING":
            time.sleep(30)
            status_job_message = self._get_status(job_id)

        if status_job_message == "SUCCESS":
            logger.info(f"Async Job {job_id} finished successfully")
            return

        logger.error("Finished with error: " + status_job_message + ". JobId: " + job_id)
        raise RuntimeError(f"Async job {job_id} finished with error: {status_job_message}")

    def _get_status(self, job_id: str) -> str:
        status_url = f"{self.domain}/clarity/v1/public/job/{job_id}/status"
        headers = self._get_headers()
        return requests.get(status_url, headers=headers).json()['statusMessage']

    def _download_job_result(self, job_id: str) -> str:
        download_url = f"{self.domain}/clarity/v1/public/job/{job_id}/fetch"
        local_filename = os.path.join(tempfile.gettempdir(), f"{job_id}.csv.gz")
        return self._download_file(download_url, local_filename)

    def _download_file(self, download_url: str, local_filename: str) -> str:
        logging.info(f"Downloading file from {download_url} to {local_filename}.")

        headers = self._get_headers()
        response = requests.get(download_url, headers=headers, stream=True)

        if response.status_code != OK:
            logger.error(f"Error getting job result: {response.status_code}")
            raise RuntimeError(f"Error downloading job content: {response.status_code}")

        with open(local_filename, 'wb') as file:
            for chunk in response.iter_content(chunk_size=self.DOWNLOAD_FILE_CHUNK_SIZE):
                file.write(chunk)

        logging.info(f"Downloaded file from {download_url} to {local_filename}. "
                     f"File size {os.path.getsize(local_filename)}")
        return local_filename

