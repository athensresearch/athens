package middleware

import (
	"bytes"
	"encoding/json"
	"net/http"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/log"
	"github.com/gomods/athens/pkg/paths"
)

// NewValidationMiddleware builds a middleware function that performs validation checks by calling
// an external webhook
func NewValidationMiddleware(entry log.Entry, validatorHook string) buffalo.MiddlewareFunc {
	const op errors.Op = "actions.ValidationMiddleware"

	return func(next buffalo.Handler) buffalo.Handler {
		return func(c buffalo.Context) error {
			sp := buffet.SpanFromContext(c).SetOperationName("validationMiddleware")
			defer sp.Finish()

			mod, err := paths.GetModule(c)

			if err != nil {
				// if there is no module the path we are hitting is not one related to modules, like /
				return next(c)
			}

			// not checking the error. Not all requests include a version
			// i.e. list requests path is like /{module:.+}/@v/list with no version parameter
			version, _ := paths.GetVersion(c)

			if version != "" {
				valid, err := validate(validatorHook, mod, version)
				if err != nil {
					entry.SystemErr(err)
					return c.Render(http.StatusInternalServerError, nil)
				}

				if !valid {
					return c.Render(http.StatusForbidden, nil)
				}
			}
			return next(c)
		}
	}
}

type validationParams struct {
	Module  string
	Version string
}

func validate(hook, mod, ver string) (bool, error) {
	const op errors.Op = "actions.validate"

	toVal := &validationParams{mod, ver}
	jsonVal, err := json.Marshal(toVal)
	if err != nil {
		return false, errors.E(op, err)
	}

	resp, err := http.Post(hook, "application/json", bytes.NewBuffer(jsonVal))
	if err != nil {
		return false, errors.E(op, err)
	}

	switch {
	case resp.StatusCode == http.StatusOK:
		return true, nil
	case resp.StatusCode == http.StatusForbidden:
		return false, nil
	default:
		return false, errors.E(op, "Unexpected status code ", resp.StatusCode)
	}
}
