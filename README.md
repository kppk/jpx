JPX is an **experimental** package management for java.

`PROOF OF CONCEPT, DO NOT USE!`

GOAL: make working with java feel fast, easy and fun!

Note: Because 'package' has special meaning in java, it uses word 'pack' -> java packs -> jpx.

### General

    - heavily inspired by Rust's cargo, npm (yarn) and elm-package
    - build on top of GRAAL and latest jdk features (native, modules, linking, jshell, jfr, etc)

### What is a jpx pack

    - has jpx.tom in root
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

    - backed up by github (might be extended to other git repos later)
    - dependency is on source (!) repository
    - on running build, fetch all dependencies to `lib` directory
    - dependency is defined as:

        [dep]
        'kppk.somelibrary' = '1.0.0 <= v < 2.0.0'

        this is translated to `https://github.com/kppk/somelibrary`, tags from 1.0.0 up to (not including) 2.0.0

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
`jpx add`             - add new dependency to jpx.toml and install it to `lib` directory



