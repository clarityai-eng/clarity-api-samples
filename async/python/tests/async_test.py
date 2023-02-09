from async_downloader import AsyncDownloader


def test_should_return_file_when_requested_to_async_endpoint():
    async_downloader = AsyncDownloader("any_key", "any_secret")
    my_file = async_downloader.download("", {})

    assert my_file
