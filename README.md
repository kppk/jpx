JPX is an **experimental** package management for java.

GOAL: make working with java feel fast, easy and fun!

Note: Because 'package' has special meaning in java, it uses word 'pack' -> java packs -> jpx.

Couple of main ideas:

###general

    - heavily inspired by Rust's cargo and npm (yarn)
    - build on top of GRAAL and latest jdk features (native, modules, linking, jshell, jfr, etc)
    
###dependency management

    - generate lock file to make build reproducible
    - use local cache in ~/.jpx/cache
    - on running build, fetch all dependencies to `lib` directory
    - support directory and git dependency
    - support for private repositories
    
###build

    - two pack's types: application, library
    - (optionally) generate JpxInfo class in main package with information from the project's jpx.toml
    
###packaging

    - possible artifacts: library -> jar, application -> native (graal or jlink)
    - allow application installation from repository (download source, build to ~/.jpx/bin), jpx install
    - secure first, OOB support for jar signing
    - jars contain source, javadoc (have you ever used library without code available?) - don't apply to application
    - jar META-INF structure:
        - jpx
            - src - source files
            - doc - javadocs
            - jpx.toml -- file used when creating this jar

    
#Commands

jpx new <project>   - create new project in provided directory
jpx init            - init project in current directory
jpx build           - build the project
jpx clean           - remove target directory
jpx dep install    
    
    
MVP scenario:

- create new binary project: `jpx new hello-world-1 --bin`    
- create another library project: `jps new hello-world-2 --lib`
- change dir to first project: `cd hello-world-1`
- put dependency on the library project: `echo '[deps]\nhello-world-2 = { path = \"../new hello-world-2\" }' >> jpx.toml`
- build binary: `jpx build`

expectation: dependent library is compiled to jar, static library `./bin/hello-world-1` is created      


    
#Implementation details:

- module name: "org.some.module-name"    
    