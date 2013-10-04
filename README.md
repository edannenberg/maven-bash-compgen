# maven-bash-compgen

Generates bash completion files for all maven plugins that support the official help mojo. Provides plugin goals and -D params to 
[maven-bash-completion](http://github.com/edannenberg/maven-bash-completion)

## Installation

1. Visit the [maven-bash-completion](http://github.com/edannenberg/maven-bash-completion) repo and follow the installation instructions.
2. Download [uberjar](http://maven.bbe-consulting.de/content/repositories/releases/de/bbe-consulting/maven/maven-bash-compgen/0.1.0/maven-bash-compgen-0.1.0-standalone.jar)

## Usage

Just run the uberjar with plugin names seperated by whitespace, the following would create config files for the help and release plugin:

    $ java -jar maven-bash-compgen-0.1.0-standalone.jar help release

If you run it frequently you may want to put something like

    $ alias mvn-bash-comp="java -jar ~/downloads/maven-bash-compgen-0.1.0-standalone.jar $1"

into your .bashrc.

## Options

Switches | Default | Desc
--- | --- | ---
 -o, --output-path | ~/.bash_completion_maven.d/ | Where to write the plugin's bash completion file(s)
 -e, --only-expressions | false | Only includes -D params that have an expression value set.
 -l, --only-locals | false | Only includes -D params that do not have an expression value set.
 -pl, --prefer-locals | false | Ignore expression value of -D param if it has one. Per default the expression value has precedence.

## License

Copyright © 2013 Erik Dannenberg

Distributed under the Eclipse Public License, the same as Clojure.
