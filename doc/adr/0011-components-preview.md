# 10. Components Preview

Date: 2021-09-09

## Status

Approved.

Uses [10. JS Components](0010-js-components.md).


## Context

We'd like to be able to see changes in our JS Components without having to build and serve the PR that introduced those changes.

A common way to do this in GitHub projects is to add CD automation to push PR builds to preview domains.

We could roll our own automation to do this over the free github pages domain but it is likely that this is tricky work.

Vercel is a popular provider of preview builds that the team already has experience with.


## Decision

Use Vercel with a single member team to reduce costs.

Use a `vercel-build` npm script to enable developers to change the build step without having access to the Vercel settings.

Set the [Production Branch](https://vercel.com/docs/git#production-branch) to `feature/rtc-v1` while that's the main development branch, and afterwards set it to `main`. 

Set the Vercel [Build & Development Settings](https://vercel.com/docs/build-step#build-&-development-settings) to:
- Build command: `yarn vercel-build`
- Output directory: `storybook-static`
- Install command: `yarn install`


## Consequences

Negative consequences:
* We have to pay for Vercel teams with at least 1 member because of the athensresearch org
  * We might be able to avail of the [Vercel OSS sponsorship](https://vercel.com/support/articles/can-vercel-sponsor-my-open-source-project)
* The preview domain is deployed even if there's no changes to the JS components build
   * Customizing the [Ignored Build Step](https://vercel.com/docs/platform/projects#ignored-build-step) might enable this, but seems error-prone
* Vercel will comment on every PR once, adding some spam to notifications'
* Vercel only allows for a single auto-deploy target branch that we have to change when we have a new major development branch
* Vercel does not allow in-repo file config for build or branch settings

Positive consequences:
* We can see and show others the components build
