# A base, just JVM
FROM openjdk:16

RUN mkdir -p /srv/athens/db

# Copy from local working directory
COPY target/athens-lan-party-standalone.jar /srv/athens/

# Set athens as the working directory
WORKDIR /srv/athens/

# Expose ports
EXPOSE 3010

# serve jar file
CMD ["script/docker-run-lan-party.sh"]

# Logging: By default docker uses the json-file driver to store container
# logs and can be found in : /var/lib/docker/containers/[container-id]/[container-id]-json.log:w
