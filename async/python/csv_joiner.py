import csv
import logging
import os
import sqlite3
import tempfile
from sqlite3 import Connection, Cursor

logger = logging.getLogger(__name__)

MAX_ROWS_PER_ITERATION = 50000


class CSVJoiner:

    connection: Connection = None

    def __init__(self, left_file: str, right_file: str):
        self.left_file = left_file
        self.right_file = right_file
        self.db_file = None

    def __enter__(self):
        self.db_file = tempfile.NamedTemporaryFile().name
        logger.info(f"File {self.left_file}. Created temp Sqlite DB file: {self.db_file}")
        self.connection = sqlite3.connect(self.db_file)
        return self

    def __exit__(self, _exc_type, _exc_val, _exc_tb):
        if self.connection:
            self.connection.close()
        if self.db_file:
            logger.info(f"Removing temp Sqlite DB file: {self.db_file}")
            os.remove(self.db_file)
            
    def join(self, key_column: str, how: str = "left") -> str:
        if not self._is_column_in_file(key_column, self.left_file):
            raise RuntimeError(f"Column to join '{key_column}' isn't in file {self.left_file}")
        if not self._is_column_in_file(key_column, self.right_file):
            raise RuntimeError(f"Column to join '{key_column}' isn't in file {self.right_file}")

        conn = self.connection
        cursor = conn.cursor()

        self._import_csv_to_sqlite(cursor, self.left_file, 'table_left')
        self._import_csv_to_sqlite(cursor, self.right_file, 'table_right')

        # Get column names
        cursor.execute('PRAGMA table_info(table_left)')
        cols1 = [row[1] for row in cursor.fetchall()]
        cursor.execute('PRAGMA table_info(table_right)')
        cols2 = [row[1] for row in cursor.fetchall()]
        # We don't want the key column twice in final result
        cols2.remove(key_column)

        new_cols = cols1 + cols2

        select_cols = [f'table_left."{col}"' for col in cols1] + [f'table_right."{col}"' for col in cols2]
        select_clause = ', '.join(select_cols)

        # Perform JOIN (can be INNER , LEFT, JOIN, etc.)
        query = f"""
            SELECT {select_clause}
            FROM table_left
            {how} JOIN table_right ON table_left."{key_column}" = table_right."{key_column}"
        """

        cursor.execute(query)

        # Write to output CSV
        output_file = tempfile.NamedTemporaryFile().name
        headers = new_cols
        with open(output_file, 'w', newline='') as f_out:
            writer = csv.writer(f_out)
            writer.writerow(headers)

            rows = cursor.fetchmany(MAX_ROWS_PER_ITERATION)
            while rows:
                writer.writerows(rows)
                rows = cursor.fetchmany(MAX_ROWS_PER_ITERATION)

        logger.info(f"Left file {self.left_file} and right file {self.right_file} were joined with strategy '{how}' into file {output_file}")
        return output_file

    def _import_csv_to_sqlite(self, cursor: Cursor, csv_file: str, table_name: str):
        with open(csv_file, newline='') as f:
            reader = csv.reader(f)
            headers = next(reader)

            columns = ', '.join([f'"{col}" TEXT' for col in headers])
            cursor.execute(f'CREATE TABLE {table_name} ({columns})')
            
            for row in reader:
                placeholders = ', '.join(['?'] * len(row))
                cursor.execute(f'INSERT INTO {table_name} VALUES ({placeholders})', row)

    def _is_column_in_file(self, column: str, file_path: str):
        with open(file_path, newline='') as f:
            reader = csv.reader(f)
            header = next(reader)
            return column in header

def join_all_files_by_column(files: list, column: str) -> str:
    left_file = None
    for file in files:
        if left_file:
            with CSVJoiner(left_file, file) as csv_joiner:
                left_file = csv_joiner.join(column)
        else:
            left_file = file
    return left_file



