# JPX

[![Build Status](https://travis-ci.org/kppk/jpx.svg?branch=master)](https://travis-ci.org/kppk/jpx)
[![Version](https://img.shields.io/github/release/kppk/jpx.svg?style=plastic)](https://github.com/kppk/jpx/releases/latest)
[![License](https://img.shields.io/github/license/kppk/jpx.svg?style=plastic)](https://github.com/kppk/jpx/blob/master/LICENSE)

JPX is an **experimental** package management for java.

`PROOF OF CONCEPT, DO NOT USE!`

GOAL: make working with java feel fast, easy and fun!

Note: Because 'package' has special meaning in java, it uses word 'pack' -> java packs -> jpx.

### General

    - heavily inspired by Rust's cargo, npm (yarn) and elm-package
    - build on top of GRAAL as native binary
    - works with java 9+
    - java module centric, dependencies are defined in `module-info.java` only
    - github support only currently

### Getting started

install:

`curl -LSs https://raw.githubusercontent.com/kppk/jpx/master/install.sh | sh`

add jpx to your PATH:

`export PATH=~/.jpx/bin:$PATH`

create new binary project:

`jpx new myorg/myrepo --bin`

`myorg` is your github organization (username)
`myrepo` is your github repository

build your new project:

```bash
cd myorg/myrepo
jpx install  ## install dependencies to ./lib
jpx build
```


    

### What is a jpx pack

    - has jpx.toml in root
    - it is source repo in github
    - directory structure:

      somelibrary
        lib  <-- place deps' source under this directory
          kppk.otherlibrary
        java
          kppk.somelibrary  <- main source code here
          kppk.somelibrary.test  <-- unit tests
          kppk.somelibrary.bench  <-- bench marks
        target  <-- build under this directory
          mods
        jpx.toml

### Version

    - pack has semantic version
    - pack starts with version 1.0.0
    - support for version bumping


### Dependency management

    - all dependencies are defined in `module-info.java` files!
    - backed up by github (might be extended to other git repos later)
    - dependency is on source (!) repository
    - `jpx isntall` will fetch all dependencies to `lib` directory


### Build

    - two pack's types: application, library
    - (optionally) generate JpxInfo class in main package with information from the project's jpx.toml

### Packaging

    - possible artifact: application -> native (graal or jlink)
    - allow application installation from repository (download source, build to ~/.jpx/bin), jpx install

### Commands

`jpx new <project>`   - create new project in provided directory

`jpx init`            - init project in current directory

`jpx build`           - build the project

`jpx clean`           - remove target directory

`jpx install`         - install all dependencies to `lib` directory




