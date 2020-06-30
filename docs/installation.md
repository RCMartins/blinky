---
id: installation
title: Installation
sidebar_label: Installation
---

## Requirements

Every pull request is tested on a Linux machine. Official support for macOS in the future.  

* **Java 8**

* **Scala @SCALA_VERSION@ or 2.13.3**

* **Sbt >= 1.3.4**

### Coursier

Start by installing [Coursier](https://get-coursier.io/docs/cli-installation) command-line interface.
(`cs` will be used in the instructions below.)

Install [Bloop](https://scalacenter.github.io/bloop/setup) using coursier:
```shell
cs setup --apps bloop
```

### Configuration file

Next, create `.blinky.conf` configuration file in the root of your project:
```hocon
projectPath = "."
projectName = "project-name"
filesToMutate = "src/main/scala"
options = {
  maxRunningTime = 10 minutes
}
```
More information about configuration [here](configuration.md).

---

Next, download _Blinky_ with coursier, and then, check the version:
```shell
cs bootstrap com.github.rcmartins:blinky-cli_2.12:@STABLE_VERSION@ -o blinky
./blinky -v     # should say v@STABLE_VERSION@
```
 
You can also launch _Blinky_ without creating an executable with:
```shell
cs launch com.github.rcmartins:blinky-cli_2.12:@STABLE_VERSION@ -- -v
```

_Blinky_ will compile the project and generate the necessary semanticdb files
before applying the mutations to the code.

You can also pass parameters directly to override parameters in the .blinky.conf file: 
```shell
cs launch com.github.rcmartins:blinky-cli_2.12:@STABLE_VERSION@ -- --onlyMutateDiff=true
```
