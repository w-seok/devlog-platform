#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    create user sonar;
    alter user sonar with password 'sonar' superuser;
    create database sonar;
    grant all privileges on database sonar to sonar;
EOSQL
