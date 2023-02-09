class AsyncDownloader:

    def __init__(self, key: str, secret: str):
        self.key = key
        self.secret = secret

    def download(self, uri: str, body: dict) -> str:
        return "any_file"
