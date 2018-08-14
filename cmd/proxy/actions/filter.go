package actions

import (
	"net/http"
	"net/url"
	"strings"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/module"
	"github.com/gomods/athens/pkg/paths"
)

func newFilterMiddleware(mf *module.Filter) buffalo.MiddlewareFunc {
	const op errors.Op = "actions.FilterMiddleware"

	return func(next buffalo.Handler) buffalo.Handler {
		return func(c buffalo.Context) error {
			sp := buffet.SpanFromContext(c).SetOperationName("filterMiddleware")
			defer sp.Finish()
			mod, err := paths.GetModule(c)

			if err != nil {
				// if there is no module the path we are hitting is not one related to modules, like /
				return next(c)
			}

			// not checking the error. Not all requests include a version
			// i.e. list requests path is like /{module:.+}/@v/list with no version parameter
			version, _ := paths.GetVersion(c)

			if isPseudoVersion(version) {
				return next(c)
			}

			rule := mf.Rule(mod)
			switch rule {
			case module.Exclude:
				return c.Render(http.StatusForbidden, nil)
			case module.Direct:
				return next(c)
			case module.Include:
				// TODO : spin up cache filling worker and serve the request using the cache
				newURL := redirectToOlympusURL(c.Request().URL)
				return c.Redirect(http.StatusSeeOther, newURL)
			}

			return next(c)
		}
	}
}

func isPseudoVersion(version string) bool {
	return strings.HasPrefix(version, "v0.0.0-")
}

func redirectToOlympusURL(u *url.URL) string {
	return strings.TrimSuffix(GetOlympusEndpoint(), "/") + u.Path
}
