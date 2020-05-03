# Contributing to Athens

If you're looking for somewhere to start contributing, check out the issues tagged with "first issue" found in [issues](https://github.com/athensresearch/athens/issues). If you have a specific feature that you would like to work on (for which there is no open issue yet), please let us know.

These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to this document in a pull request.

Before contributing, please first read the [Code of Conduct](https://github.com/athensresearch/athens/blob/master/CODE_OF_CONDUCT.md).

## Communication Channels

Most communication about Athens development happens in the #athens channel in the [Roam Slack](https://roamresearch.slack.com/join/shared_invite/enQtODg3NjIzODEwNDgwLTdhMjczMGYwN2YyNmMzMDcyZjViZDk0MTA2M2UxOGM5NTMxNDVhNDE1YWVkNTFjMGM4OTE3MTQ3MjEzNzE1MTA). 

You can also reach out to Jeff on Twitter at [@tangjeff0](https://twitter.com/tangjeff0). He posts regular updates about this project. We will make an Athens Research Twitter account later at a suitable time. 

## Development Environment

1. Download a `java` JDK. You can download the most [current version](https://www.oracle.com/java/technologies/javase-downloads.html) or access the [JDK archives](https://jdk.java.net/archive/).
2. Download `yarn`: [yarn](https://www.npmjs.com/package/yarn). 
3. Install `lein` (build automation and dependency management tool for Clojure). You can either use a [package manager](https://github.com/technomancy/leiningen/wiki/Packaging) such as Homebrew or Chocolatey, or you can download from the command line. Detailed instructions for installing from the command line on Linux, macOS, and Windows are below.

### On Linux:
   * Install curl command
> ```sudo apt-get install -y curl```
   * Download the lein script
> ```curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > lein```
   * Move the lein script to the user programs directory
> ```sudo mv lein /usr/local/bin/lein```
   * Add execute permissions to the lein script
> ```sudo chmod a+x /usr/local/bin/lein```
   * Verify your installation
> ```lein version```
   
   It should take a while to run, as it will download some resources it needs the first time. See the note at the end of this section if you are having issues.

### On macOS:
   * Download the lein script
> ```curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > lein```
   * Move the lein script to the user programs directory
> ```sudo mv lein /usr/local/bin/lein```
   * Add execute permissions to the lein script
> ```sudo chmod a+x /usr/local/bin/lein```
   * Verify your installation
> ```lein version```

   It should take a while to run, as it will download some resources it needs the first time. See the note at the end of this section if you are having issues.

### On Windows:
   * Download the lein.bat script
> ```curl -O https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein.bat```
   * Create a bin directory for scripts
> ```md bin```
   * Move the lein.bat script to that directory
> ```move lein.bat bin```
   * Add bin to your path
> ```setx path "%path%;%USERPROFILE%\bin"```
   * Complete your installation
   Close the command prompt and open a new one. Then run the following command to finish the installation.
> ```lein self-install```
   
   It should take a while to run, as it will download some resources it needs the first time. See the note at the end of this section if you are having issues.

4. Clone the repo found [here](https://github.com/athensresearch/athens). Change directory to the athens folder and run:
> ```yarn install```

   After installing all packages and dependencies, start leiningen.
> ```lein dev```

   Open [localhost:3000](http://localhost:3000) in your browser and you should be good to go!

### Trouble Setting Up Your Dev Environment?
If you are having trouble getting your dev environment set up, first go through the steps found [here](https://purelyfunctional.tv/guide/how-to-install-clojure/). If you are still having trouble, please let us know in the #athens channel in the Roam Slack.

## Clojure

Until stated otherwise, we will be following [this style guide](https://github.com/bbatsov/clojure-style-guide). ([This version](https://guide.clojure.style) is prettier to look at.) This guide is still a work in progress; from the author: "Feel free to [open tickets or send pull requests](https://github.com/bbatsov/clojure-style-guide/issues) with improvements." Exceptions to this style guide will be collated and organized as needed. Feel free to suggest exceptions that you feel strongly about.  

We will soon likely setup [clj-kondo](https://github.com/borkdude/clj-kondo) with some config as a project linter. The clj-kondo linter follows the style guidelines in the above style guide.

## Git Commit Messages 

Follow suggestions in https://www.conventionalcommits.org/en/v1.0.0/. 

## Issues and PRs

Follow suggestions in https://github.com/codeforamerica/howto/blob/master/Good-GitHub-Issues.md


