package cdn

// Getter gets the details about a given baseURL/module at vsn and returns
// the base URL of the module metadata & content:
//
// - {baseURL}/{module}/@v/list
// - {baseURL}/{module}/@v/{version}.info
// - {baseURL}/{module}/@v/{version}.mod
// - {baseURL}/{module}/@v/{version}.zip
type Getter interface {
	Get(baseURL, module, vsn string) (string, error)
}
