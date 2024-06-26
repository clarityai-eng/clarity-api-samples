import json
import logging
import os
import tempfile

import requests
import time

from http.client import OK
from enum import Enum
from typing import Any

logger = logging.getLogger(__name__)


class JobStatus(Enum):
    RUNNING = "RUNNING"
    SUCCESS = "SUCCESS"

    @property
    def is_running(self) -> bool:
        return self == JobStatus.RUNNING

    @property
    def is_success(self) -> bool:
        return self == JobStatus.SUCCESS


class AsyncDownloader:
    DOWNLOAD_FILE_CHUNK_SIZE = 8192

    def __init__(self, key: str, secret: str, domain: str = "https://api.clarity.ai"):
        self.domain = domain
        self.key = key
        self.secret = secret
        self.token: str | None = None

    def download(self, uri: str, body: dict) -> str:
        job_id = self._request_async(uri, body)
        self._wait_for_job(job_id)
        return self._download_job_result(job_id)

    def _request_async(self, uri: str, data: dict) -> str:
        url = f"{self.domain}/clarity/v1/public{uri}"
        logger.info(f"Requesting Job to '{url}' with data {data}")

        headers = self._headers()
        response = requests.post(url, headers=headers, json=data)
        content = response.json()

        if not isinstance(content, dict):
            logger.error(f"Error requesting async: {content}")
            raise RuntimeError(f"Error requesting async job: {content}")

        if "message" in content:
            error_message = content["message"]
            logger.error(f"Error requesting async: {error_message}")
            raise RuntimeError(f"Error requesting async job: {error_message})")

        if "elements" in content:
            error_message = content["elements"][0]["message"]
            logger.error(f"Error requesting async: {error_message}")
            raise RuntimeError(f"Error requesting async job: {error_message})")

        uuid = content["uuid"]
        logger.info(f"Requested Job with UUID: {uuid}")
        return uuid

    @staticmethod
    def _success_login(response: Any) -> bool:
        return isinstance(response, dict) and "token" in response

    def _wait_for_job(self, job_id: str) -> None:
        logger.info(f"Waiting for Job {job_id} to finish...")
        job_status = self._get_status(job_id)

        while job_status.is_running:
            time.sleep(30)
            job_status = self._get_status(job_id)

        if job_status.is_success:
            logger.info(f"Async Job {job_id} finished successfully")
            return

        logger.error(f"Finished with error: {job_status.value}. JobId: {job_id}")
        raise RuntimeError(f"Async job {job_id} finished with error: {job_status.value}")

    def _get_status(self, job_id: str) -> JobStatus:
        url = f"{self.domain}/clarity/v1/public/job/{job_id}/status"
        headers = self._headers()
        response = requests.get(url, headers=headers)

        if response.status_code == 401:
            # Token is expired. Request a new one
            self.token = None
            headers = self._headers()
            response = requests.get(url, headers=headers)
            # Now the response code must be 202, otherwise
            # something else is wrong with the authentication
            if response.status_code not in [202, 302]:
                raise RuntimeError("Couldn't renew the authentication token correctly")

        content = response.json()
        status = content["statusMessage"]
        return JobStatus(status)

    def _download_job_result(self, job_id: str) -> str:
        url = f"{self.domain}/clarity/v1/public/job/{job_id}/fetch"
        filename = os.path.join(tempfile.gettempdir(), f"{job_id}.csv.gz")
        return self._download_file(url, filename)

    def _download_file(self, url: str, filename: str) -> str:
        logging.info(f"Downloading file from {url} to {filename}.")

        headers = self._headers()
        response = requests.get(url, headers=headers, stream=True)

        if response.status_code != OK:
            logger.error(f"Error getting job result: {response.status_code}")
            raise RuntimeError(f"Error downloading job content: {response.status_code}")

        with open(filename, "wb") as file:
            for chunk in response.iter_content(chunk_size=self.DOWNLOAD_FILE_CHUNK_SIZE):
                file.write(chunk)

        logging.info(f"Downloaded file from {url} to {filename}. " f"File size {os.path.getsize(filename)}")
        return filename

    def _headers(self) -> dict:
        return {"Content-Type": "application/json", "Authorization": f"Bearer {self._get_token()}"}

    def _get_token(self) -> str:
        if not self.token:
            self.token = self._request_new_token()
        return self.token

    def _request_new_token(self) -> str:
        logging.info(f"Requesting new token...")

        body = {"key": self.key, "secret": self.secret}
        response = requests.post(f"{self.domain}/clarity/v1/oauth/token", json=body)

        content = response.json()
        if self._success_login(content):
            return content["token"]

        if isinstance(content, dict):
            error_message = content.get("status") or content["message"]
            logger.error(f"Unable to get token: {error_message}")
            raise RuntimeError(f"Cannot get authentication token for Public API: {error_message})")

        logger.error(f"Unable to get token: {content}")
        raise RuntimeError(f"Cannot get authentication token for Public API: {content})")
