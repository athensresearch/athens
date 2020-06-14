FROM theasp/clojurescript-nodejs:latest
WORKDIR /usr/src/app
ARG http_proxy
COPY package.json yarn.lock /usr/src/app/
RUN yarn
COPY project.clj /usr/src/app/project.clj
RUN lein deps
COPY . /usr/src/app
RUN lein do compile
CMD ["lein", "dev"]
EXPOSE 3000 8777 9630
