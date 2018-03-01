from golang:1.7

RUN echo $GOPATH
RUN mkdir -p $GOPATH/src/github.com/gobuffalo/pop
WORKDIR $GOPATH/src/github.com/gobuffalo/pop
ADD . .
RUN go get -t -v ./...
RUN SODA_DIALECT=sqlite go test ./...
