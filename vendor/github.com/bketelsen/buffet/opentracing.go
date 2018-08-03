package buffet

import (
	"context"
	"strings"

	"github.com/gobuffalo/buffalo"
	"github.com/opentracing/opentracing-go"
	"github.com/opentracing/opentracing-go/ext"
	olog "github.com/opentracing/opentracing-go/log"
)

var tracer opentracing.Tracer

// OpenTracing is a buffalo middleware that adds the necessary
// components to the request to make it traced through OpenTracing.
// Initialize it by passing in an opentracing.Tracer.
func OpenTracing(tr opentracing.Tracer) buffalo.MiddlewareFunc {
	tracer = tr
	return func(next buffalo.Handler) buffalo.Handler {
		return func(c buffalo.Context) error {
			opName := "HTTP " + c.Request().Method + c.Request().URL.Path

			rt := c.Value("current_route")
			if rt != nil {
				route, ok := rt.(buffalo.RouteInfo)
				if ok {
					opName = operation(route.HandlerName)
				}
			}

			wireContext, _ := tr.Extract(
				opentracing.HTTPHeaders,
				opentracing.HTTPHeadersCarrier(c.Request().Header))

			// Create the span referring to the RPC client if available.
			// If wireContext == nil, a root span will be created.
			sp := tr.StartSpan(
				opName,
				ext.RPCServerOption(wireContext))

			ext.HTTPMethod.Set(sp, c.Request().Method)
			ext.HTTPUrl.Set(sp, c.Request().URL.String())

			ext.Component.Set(sp, "buffalo")
			c.Set("otspan", sp)
			err := next(c)
			if err != nil {
				ext.Error.Set(sp, true)
				sp.LogFields(olog.Error(err))
			}
			br, ok := c.Response().(*buffalo.Response)
			if ok {
				ext.HTTPStatusCode.Set(sp, uint16(br.Status))
			}
			sp.Finish()
			return err
		}
	}
}

// SpanFromContext attempts to retrieve a span from the Buffalo context,
// returning it if found.  If none is found a new one is created.
func SpanFromContext(c buffalo.Context) opentracing.Span {
	// fast path - find span in the buffalo context and return it
	sp := c.Value("otspan")
	if sp != nil {
		span, ok := sp.(opentracing.Span)
		if ok {
			c.LogField("span found", true)
			return span
		}
	}

	c.LogField("span found", false)
	// none exists, make a new one (sadface)
	opName := "HTTP " + c.Request().Method + c.Request().URL.Path

	rt := c.Value("current_route")
	if rt != nil {
		route, ok := rt.(buffalo.RouteInfo)
		if ok {
			opName = operation(route.HandlerName)
		}
	}
	span := tracer.StartSpan(opName)
	ext.HTTPMethod.Set(span, c.Request().Method)
	ext.HTTPUrl.Set(span, c.Request().URL.String())

	ext.Component.Set(span, "buffalo")
	return span

}

// ChildSpan returns a child span derived from the buffalo context "c"
func ChildSpan(opname string, c buffalo.Context) opentracing.Span {
	psp := SpanFromContext(c)
	sp := tracer.StartSpan(
		opname,
		opentracing.ChildOf(psp.Context()))
	return sp
}

func operation(s string) string {
	chunks := strings.Split(s, ".")
	return chunks[len(chunks)-1]
}

// ChildSpanFromContext takes an opname and context.Context and returns a span
// NB: Using this function will not mean that buffalo metadata won't be attached to the traces in the new Span
func ChildSpanFromContext(opname string, ctx context.Context) opentracing.Span {
	psp := opentracing.SpanFromContext(ctx)
	sp := tracer.StartSpan(
		opname,
		opentracing.ChildOf(psp.Context()))
	return sp
}
