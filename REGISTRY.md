# The Athens Registry

Athens runs a registry service that stores module source code and metadata for
a wide variety of public Go code. Fundamentally, the registry serves module
metadata and source code, but it has several important features on top of
this basic functionality:

* It is hosted and stores module metadata and source code in a CDN
  * It uses `<meta>` redirects to tell `go get` to fetch module metadata and
    source code from the CDN
* Modules are identified by domain name
* The registry is available on several domain names
  * For example, one of the domain names is `gomods.io`, and so a valid
    module name is `gomods.io/module/one`
* It has an API for uploading new modules -- and new versions of existing
  module -- to the registry
  * You have to be authenticated and authorized to upload
  * Authentication is done with Github login (other login systems may be
    supported in the future)
* It is capable of verifying module source code integrity
* The registry names modules according to the `module "abc"` directive
  in the `go.mod` file. The custom module naming enables custom import
  directives like this:

  ```go
  package mycustompackage // import "my/custom/package"
  ```
