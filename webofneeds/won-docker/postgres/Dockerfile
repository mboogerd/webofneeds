# This image is build and pushed to the dockerhub manually (webofneeds/postgres)
# The postgres server should by default be accessible at port 5432

# use java as a base image
FROM postgres:9.5

# set the won user
ENV POSTGRES_PASSWORD=won
ENV POSTGRES_USER=won

# init the database for won node and owner
ADD ./default_init.sql /docker-entrypoint-initdb.d/default_init.sql
ADD ./test_init.sql /docker-entrypoint-initdb.d/test_init.sql

# activate more logging
CMD [ "postgres", "-c", "log_destination=stderr, syslog", "-c", "logging_collector=on", "-c", "log_statement=ddl"]
