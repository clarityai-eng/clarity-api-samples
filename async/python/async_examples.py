import logging
import os

from async_downloader import AsyncDownloader

logger = logging.getLogger(__name__)

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s,%(msecs)03d %(levelname)s %(module)s - %(funcName)s: %(message)s",
    datefmt="%Y-%m-%dT%H:%M:%S",
)

DEFAULT_KEY = "YOUR_KEY_HERE"
DEFAULT_SECRET = "YOUR_SECRET_HERE"

KEY = os.getenv("CLARITY_AI_API_KEY", default=DEFAULT_KEY)
SECRET = os.getenv("CLARITY_AI_API_SECRET", default=DEFAULT_SECRET)

async_downloader = AsyncDownloader(KEY, SECRET)

logger.info(f"Requesting some ESG Risk data for the whole universe of Equities...")
body = {"scoreIds": ["ESG", "ENVIRONMENTAL"], "securityTypes": ["EQUITY"]}
result_file = async_downloader.download("/securities/esg-risk/scores-by-id/async", body)
logger.info(f"File with ESG Risk data for Equities: {result_file}")


logger.info(f"Requesting SFDR Data for the whole universe of Organizations...")
body = {"metricIds": ["CARBON_FOOTPRINT", "GHG_INTENSITY"]}
result_file = async_downloader.download("/organizations/sfdr/metric-by-id/async", body)
logger.info(f"File with SFDR data for Organizations: {result_file}")
