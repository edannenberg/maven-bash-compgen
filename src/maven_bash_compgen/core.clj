(ns ^{:author "Erik Dannenberg"}
  maven-bash-compgen.core
  (:require clojure.tools.cli)
  (:require [clojure.java.shell :only [sh] :as shell])
  (:require [clojure.string :only [split replace] :as string])
  (:gen-class))

(def ^:dynamic prefer-locals false)
(def ^:dynamic only-expressions false)
(def ^:dynamic only-locals false)

(defn rm-last-chr
  "Returns s with it's last char stripped."
  [s]
  (if (> (count s) 0) (subs s 0 (- (count s) 1)) s))

(defn get-plugin-goals
  "Returns all goals of plugin by running mvn plugin:help"
  [plugin]
  (let [regex (re-pattern (apply str "(?m)^" plugin ":(.+)"))]
   (re-seq regex (:out (shell/sh "mvn" (apply str plugin ":help"))))
  ))

(defn get-goal-params
  "Returns all parameters of plugin:goal by running mvn plugin:help -Dgoal=goal -Ddetails"
  [plugin
   goal]
  (let [regex (re-pattern "(?m)^\\s{4}[^mvn](\\S+)[\\s\\S]*?$(?:\\s{7}[^Expression].+$){1,10}(?:\\s+Expression:\\s\\$\\{(.+?)\\})?")
        results (re-seq regex (:out (shell/sh "mvn" (apply str plugin ":help") "-Ddetail" (apply str "-Dgoal=" goal))))
        parsed-results (map #(cond
                               (and only-expressions (nil? (nth %1 2))) nil
                               (and only-locals (not (nil? (nth %1 2)))) nil
                               :else (list (nth %1 1) (nth %1 2)))
                            results)
        ]
    (filter #(not (nil? %1)) parsed-results) 
  ))

(defn get-formated-params
  "Returns formated -D params."
  [plugin goal]
  (let [params (get-goal-params plugin goal)
        fp (rm-last-chr (apply str (map #(if (and (not (nil? (nth %1 1))) (not prefer-locals))
                                           (str "-D" (nth %1 1) "|")
                                           (str "-D" (nth %1 0) "|")) params)))
        ]
    (apply str "local plugin_goal_params_" plugin "_" (string/replace goal #"-" "_") "=\"" fp "\"\n" )
  ))

(defn create-comp-conf
  "Returns the complete bash completion config of plugin."
  [plugin]
  (let [goals (get-plugin-goals plugin)
        formated-goals (str "local plugin_goals_" plugin "=\""(rm-last-chr (apply str (map #(apply str (nth %1 0) "|") goals))) "\"\n")
        formated-params (pmap #(get-formated-params plugin (nth %1 1)) goals) 
        ]
    (apply str formated-goals formated-params)
    ))

(defn write-conf-file
  "Write output to file, provides basic parsing for / and ~. Prolly not waterproof."
  [path
   file-name
   output
   ]
  (let [user-home (java.lang.System/getProperty "user.home")
        parsed-start (if (.startsWith path "~/") (str user-home (subs path 1)) path)
        parsed-path (if (.endsWith path "/") parsed-start (str parsed-start "/"))
        abs-path (str parsed-path file-name)
        _ (.mkdirs (clojure.java.io/file parsed-path))
        ]
    (spit abs-path output)
    ))

(defn -main
  "CLI handling."
  [& args]
  (let [[options arg help] (clojure.tools.cli/cli args 
                               "\nParses goals and -D params of given maven plugin(s) and creates a bash completion file."
                               ["-o" "--output-path" "Where to write the plugin's bash completion file(s) " :default "~/.bash_completion_maven.d/"]
                               ["-e" "--only-expressions" "Only includes -D params that have an expression value." :flag true]
                               ["-l" "--only-locals" "Only includes -D params that do not have an expression value." :flag true]
                               ["-pl" "--prefer-locals" "Ignore expression value of -D param if it has one. Per default the expression value has precedence." :flag true]
                               )
        ]
    (when-not (first arg)
      (println help)
      (System/exit 0))
    (alter-var-root #'only-expressions (constantly (:only-expressions options)))
    (alter-var-root #'only-locals (constantly (:only-locals options)))
    (alter-var-root #'prefer-locals (constantly (:prefer-locals options)))
    (println "Parsing all goals and -D params for plugin(s):" arg)
    (println "Writing output to: " (:output-path options)) 
    (doseq [plugin arg]
            (println "Processing plugin: " plugin)
            (if (nil? (re-seq #"(?m)^\[ERROR\]\sNo\sp" (:out (shell/sh "mvn" (apply str plugin ":help")))))
              (write-conf-file (:output-path options) plugin (create-comp-conf plugin))
              (println "..Error: plugin not found or without help mojo. Skipping.")
              )
            )
    (println "done.")
    (System/exit 0)))
