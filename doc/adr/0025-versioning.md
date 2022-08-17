# Versioning

## Version Numbers

Athens uses [Semver](https://semver.org/).
We use the `alpha`, `beta`, `rc` labels for pre-releases.
Large enough feature changes will increment the major number.

We place a high premium on backwards compatibility.
You should always be able to go from one version to a higher stable version without losing data.
We will migrate your data automatically when there's a breaking change.

Pre-releases aren't backwards compatible, but we try to not break anything between them because we use them ourselves.

We don't support going from a higher version to a lower version without data or functionality loss though.
The best we can guarantee here is that we will try to identify this happens, and fail gracefully.


## Support

Our support strategy is chosen to match our current development manpower.

New features happen on the latest versions.

Older versions get critical bug fixes, but no new features.
We consider a bug fix to be critical if Athens won't work at all without it.


## Release artifacts

Releases are automatically created on https://github.com/athensresearch/athens/releases.
Pre-releases are tagged in front of the version name.

Each release contains the major parts of Athens:
- desktop client
- web client
- server

We deploy the web client automatically to the following domains:
- latest release at https://web.athensresearch.org
- latest pre-release at https://beta.athensresearch.org
- latest development at https://dev.athensresearch.org

We deploy development web clients on GitHub PRs.
A link to the development deploy will show up automatically as a comment on the PR.

The desktop client will auto update in the background and notify users that there's an update.
Releases will update to the next higher version number, ignoring pre-releases if you're not on a pre-release.
The web client will auto-update on refresh for its domain.
