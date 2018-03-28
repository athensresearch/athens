package actions

import (
	"log"
	"time"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	opentracing "github.com/opentracing/opentracing-go"
	"github.com/uber/jaeger-client-go/config"
	"github.com/uber/jaeger-client-go/rpcmetrics"
	"github.com/uber/jaeger-lib/metrics/prometheus"
)

func initializeTracing(app *buffalo.App) {
	cfg := config.Configuration{
		Sampler: &config.SamplerConfig{
			Type:  "const",
			Param: 1,
		},
		Reporter: &config.ReporterConfig{
			LogSpans:            true,
			BufferFlushInterval: 1 * time.Second,
			LocalAgentHostPort:  "0.0.0.0:6831", //hostPort,
		},
	}
	// TODO(ys) a quick hack to ensure random generators get different seeds, which are based on current time.
	time.Sleep(100 * time.Millisecond)
	metricsFactory := prometheus.New()
	tracer, _, err := cfg.New(
		"athens", //serviceName,

		config.Observer(rpcmetrics.NewObserver(metricsFactory, rpcmetrics.DefaultNameNormalizer)),
	)
	if err != nil {
		log.Fatal("cannot initialize Jaeger Tracer", err)
	}
	opentracing.SetGlobalTracer(tracer)
	app.Use(buffet.OpenTracing(tracer))

}
