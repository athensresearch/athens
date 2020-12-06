ARG http_proxy
FROM ardoq/leiningen:jdk11-2.9.4
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
RUN apt-get update
RUN apt-get install -y wget gnupg git
RUN wget -qO- https://raw.githubusercontent.com/creationix/nvm/v0.33.8/install.sh | bash
RUN . ~/.profile
RUN nvm install 12.20.0
RUN curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add -
RUN echo "deb https://dl.yarnpkg.com/debian/ stable main" | sudo tee /etc/apt/sources.list.d/yarn.list
RUN apt-get update
RUN sudo apt-get install -y --no-install-recommends yarn
RUN git clone --depth 1 https://github.com/athensresearch/athens.git
RUN yarn
RUN lein deps
RUN lein do compile
CMD ['lein', 'dev']
EXPOSE 3000 8777 9630
