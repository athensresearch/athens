# Google Cloud Storage Driver

This driver provides support for storing module files in Google Cloud storage.

# Configuration

> NOTE: The GCP storage driver currently only supports the _uploading_ of modules and so can not be used at this time as a storage back end.

Minimal configuration is needed, just the name of a storage bucket and an authentication method for that project, and then tell Athens you want to use that as your storage medium.

## Driver Configuration

You will need to set an environment variable for the bucket name.
`ATHENS_STORAGE_GCP_BUCKET` should be set to the name of the bucket you wish to use. It should be something like `fancy-pony-339288.appspot.com`.

The only currently supported authentication type is a service account key file in json format.
For more information on appengine service accounts see [here](https://cloud.google.com/iam/docs/service-accounts).

The service account requires a minimum of `Storage Object Creator` level of permission for the project on GCP.
This path to this file must be set in the environment variable `ATHENS_STORAGE_GCP_SA`.

## Athens Configuration

> NOTE: Again, this is not yet implemented.
In order to tell Olympus to use GCP storage set `ATHENS_STORAGE_TYPE` to `gcp`.

# Contributing

If you would like to contribute to this driver you will need a service account for the test project in order to run tests.

Please contact [robbie](https://github.com/robjloranger) for access.
