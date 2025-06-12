import logging
import os

from async_downloader import AsyncDownloader
from csv_joiner import join_all_files_by_column
from file_utils import decompress_gzipped_file_to_tmp

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

# Request data for several modules and merge the results in one file
logger.info(f"Requesting some ESG Risk data for the whole universe of Organizations...")
body = {"scoreIds": ["ESG", "ENVIRONMENTAL"]}
esg_risk_orgs_data_gzip = async_downloader.download("/organizations/esg-risk/scores-by-id/async", body)
esg_risk_orgs_data = decompress_gzipped_file_to_tmp(esg_risk_orgs_data_gzip)
logger.info(f"File with ESG Risk data for Organizations: {esg_risk_orgs_data}")

logger.info(f"Requesting SFDR Data for the whole universe of Organizations...")
body = {"metricIds": ["CARBON_FOOTPRINT", "GHG_INTENSITY"]}
sfdr_orgs_data_gzip = async_downloader.download("/organizations/sfdr/metric-by-id/async", body)
sfdr_orgs_data = decompress_gzipped_file_to_tmp(sfdr_orgs_data_gzip)
logger.info(f"File with SFDR data for Organizations: {sfdr_orgs_data}")

# Merge by common ID (PERMID)
final_result = join_all_files_by_column([esg_risk_orgs_data, sfdr_orgs_data], "PERMID")
logger.info(f"File with ESG Risk and SFDR data for Organizations: {final_result}")
