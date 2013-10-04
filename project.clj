(defproject maven-bash-compgen "0.1.0"
  :description "Generate bash completion file for maven plugins."
  :url "http://github.com/edannenberg"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.2.4"]]
  :main maven-bash-compgen.core
  :profiles {:uberjar {:aot :all}})
