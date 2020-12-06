ARG http_proxy
FROM ardoq/leiningen:jdk11-2.9.4
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
RUN apt-get update

# install nvm
# Replace shell with bash so we can source files
RUN rm /bin/sh && ln -s /bin/bash /bin/sh

# Set debconf to run non-interactively
RUN echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections

# Install base dependencies
RUN apt-get update && apt-get install -y -q --no-install-recommends \
        apt-transport-https \
        build-essential \
        ca-certificates \
        curl \
        git \
        libssl-dev \
        wget \
    && rm -rf /var/lib/apt/lists/*

ENV NVM_DIR /usr/local/nvm # or ~/.nvm , depending
ENV NODE_VERSION 12.20.0

RUN curl -sL https://deb.nodesource.com/setup_12.x | bash -
RUN apt-get install -y nodejs

RUN curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add -
RUN echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list
RUN apt-get update
RUN apt-get install -y --no-install-recommends yarn
RUN git clone --depth 1 https://github.com/athensresearch/athens.git .
#RUN cd athens
RUN yarn
COPY project.clj /usr/src/app/project.clj
RUN lein do compile
CMD ["lein", "dev"]
EXPOSE 3000 8777 9630
