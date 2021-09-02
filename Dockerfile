# A base, just JVM
FROM clojure:openjdk-16-lein

#RUN mkdir -p /srv/athens/db
RUN ls
# Build Uberjar
RUN lein uberjar

# Copy from local working directory
#COPY target/athens-lan-party-standalone.jar /srv/athens/

# Set athens as the working directory
#WORKDIR /srv/athens/

# Expose ports
#EXPOSE 3010

# serve jar file
#CMD ["java", "-jar", "athens-lan-party-standalone.jar"]

# Logging: By default docker uses the json-file driver to store container
# logs and can be found in : /var/lib/docker/containers/[container-id]/[container-id]-json.log:w
