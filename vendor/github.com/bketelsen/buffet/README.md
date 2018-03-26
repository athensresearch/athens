## buffet
[![Build Status](https://ci.ketelsen.house/api/badges/bketelsen/buffet/status.svg)](https://ci.ketelsen.house/api/badges/bketelsen/buffet)


Buffet is an OpenTracing middleware for [buffalo](gobuffalo.io)

## Usage

In your main:

```
	tracer, closer := initTracer()
	defer closer.Close()
	opentracing.SetGlobalTracer(tracer)

	fmt.Println(tracer)
	app := actions.App(tracer)
```

initTracer looks like this:
```
func initTracer() (opentracing.Tracer, io.Closer) {
	sampler := jaeger.NewConstSampler(true)
	transport, err := udp.NewUDPTransport("", 0)
	if err != nil {
		log.Fatal(err)
	}
	reporter := jaeger.NewRemoteReporter(transport)

	tracer, closer := jaeger.NewTracer(ServiceName, sampler, reporter)
	return tracer, closer

}
```

Change your App() function to accept your tracer for initialization, and add
the middleware:

```
func App(tracer opentracing.Tracer) *buffalo.App {
	if app == nil {
    ...
		app.Use(buffet.OpenTracing(tracer))
    ...
```

Then instrument your handlers:

```
// HomeHandler is a default handler to serve up
// a home page.
func HomeHandler(c buffalo.Context) error {
	slow(c)
	return c.Render(200, r.HTML("index.html"))
}

//BadHandler returns an error
func BadHandler(c buffalo.Context) error {
	return c.Error(401, errors.New("Unauthorized!"))
}
func slow(c buffalo.Context) {
	sp := buffet.ChildSpan("slow", c)
	defer sp.Finish()
	time.Sleep(1 * time.Millisecond)
}
```

HomeHandler and BadHandler are automatically instrumented because 
of the middleware.  The slow() function isn't, so we pass in the buffalo 
context and derive a child span from the one in the buffalo context.  This
creates a child span that belongs to the parent (which was created automatically in 
the middleware).


