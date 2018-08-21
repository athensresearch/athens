FROM golang:1.11-rc

RUN mkdir -p $GOPATH/src/github.com/gomods/athens
WORKDIR $GOPATH/src/github.com/gomods/athens

COPY . .

RUN CGO_ENABLED=0 go build -o /bin/app ./cmd/proxy

ENV GO_ENV=production

EXPOSE 3000

ENTRYPOINT ["/bin/app"]
