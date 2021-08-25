# A base liningen container with java
FROM ardoq/leiningen:jdk11-2.9.4-jdk

# Install git
RUN apt-get update && apt-get install -y git

# clone the repo
RUN git clone https://github.com/athensresearch/athens.git

# Set athens as the working directory
WORKDIR /athens/

# checkout RTC branch
RUN git checkout feature/rtc-v1

# make jar file
RUN lein uberjar

# Environment Variables

# Expose ports
EXPOSE 3010   # TODO This should be set via environment variable

# serve jar file
CMD ["java", "-jar", "target/athens-lan-party-standalone.jar"]

# Output logs to local disk volume