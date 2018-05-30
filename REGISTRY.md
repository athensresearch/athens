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

# Registry Restrictions

The registry gets its code from one of two places:

* A webhook that fetches code from a VCS
* A manual upload

In either case, it imposes the following restrictions on the modules it
holds:

* In the webhook case, the VCS repository must already be "known"
  (the owner of the repository will need to register it prior)
* In either case, the registry will reject an update on an existing tag, if
  it's been downloaded 1 or more times
  * So that tags are immutable, and we don't break anyone's builds
