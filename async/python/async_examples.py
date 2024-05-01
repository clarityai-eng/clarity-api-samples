import logging

from async_downloader import AsyncDownloader

logger = logging.getLogger(__name__)
logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s,%(msecs)03d %(levelname)s %(module)s - %(funcName)s: %(message)s',
                    datefmt='%Y-%m-%dT%H:%M:%S')


KEY: str = "YOUR_KEY_HERE"
SECRET: str = "YOUR_SECRET_HERE"

async_downloader = AsyncDownloader(KEY, SECRET)

logger.info(f"Requesting some ESG Risk data for the whole universe of Equities...")
result_file = async_downloader.download("/securities/esg-risk/scores-by-id/async",
                                        {"scoreIds": ["ESG", "ENVIRONMENTAL"],
                                         "securityTypes": ["EQUITY"]})
logger.info(f"File with ESG Risk data for Equities: {result_file}")


logger.info(f"Requesting SFDR Data for the whole universe of Organizations...")
result_file = async_downloader.download("/organizations/sfdr/metric-by-id/async",
                                        {"metricIds": ["CARBON_FOOTPRINT", "GHG_INTENSITY"]})
logger.info(f"File with SFDR data for Organizations: {result_file}")
