import gzip
import logging
import shutil
import tempfile

logger = logging.getLogger(__name__)

def decompress_gzipped_file_to_tmp(filename) -> str:
    logger.info(f"Decompressing gzipped file {filename}")
    tmp_file = tempfile.NamedTemporaryFile(delete=False)
    with gzip.open(filename, 'rb') as f_in, open(tmp_file.name, 'wb') as f_out:
        shutil.copyfileobj(f_in, f_out)
    logger.info(f"Finished decompressing gzipped file {filename} to file {tmp_file.name}")
    return tmp_file.name
