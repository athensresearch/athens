---
title: "Athens 101"
date: 2018-02-11T16:59:56-05:00
---

## What is Athens?

Shortly: Athens is a project building on top of vgo (or go1.11+) trying to bring dependencies closer to you so you can count on repeatable builds even at a time when VCS is down.

The big goal of Athens is to provide a new place where dependencies — not code — live. Dependencies are immutable blobs of code and associated metadata that come from Github. They live in storage that Athens controls.

You probably already know what “immutable” means, but let me just point it out again because it’s really important for this whole system. When folks change their packages, iterate, experiment, or whatever else, code on Athens won’t change. If the package author releases a new version, Athens will pull that down and it’ll show up. So if you depend on package M version v1.2.3, it will never change on Athens. _Not even after force push, not even after repo cease to exist_.