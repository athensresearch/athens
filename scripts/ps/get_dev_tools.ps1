# install_dev_deps.ps1
# Ensure that the tools needed to build locally are present

& go get github.com/golang/lint/golint
& go get github.com/golang/dep/cmd/dep
& go get github.com/gobuffalo/buffalo/buffalo
