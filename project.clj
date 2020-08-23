(defproject flashcards "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs "2.10.19"]
                 [reagent "0.10.0"]
                 [re-frame "1.0.0"]
                 [re-com "2.8.0"]
                 [garden "1.3.10"]
                 [ns-tracker "0.4.0"]
                 [compojure "1.6.1"]
                 [ring "1.8.1"]

                 ;; My stuff
                 [aero "1.1.6"]
                 #_[com.taoensso/timbre "4.10.0"]
                 [org.clojars.sean-vincent/clj-util "0.2.2"]
                 [bidi "2.1.6"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [honeysql "1.0.444"]
                 [ragtime "0.8.0"]
                 #_[org.xerial/sqlite-jdbc "3.32.3.2"]
                 [org.postgresql/postgresql "42.2.16"]
                 [cljs-http "0.1.46"]
                 [org.clojure/core.async "1.3.610"]
                 ]

  :plugins [[lein-shadow "0.2.0"]
            [lein-garden "0.3.0"]
            [lein-shell "0.5.0"]
            [refactor-nrepl "2.5.0"]
            [cider/cider-nrepl "0.25.3"]]

  :min-lein-version "2.9.0"

  :jvm-opts ["-Xmx1G"]

  :source-paths ["src/clj" "src/cljs" "src/cljc"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "resources/public/css"]


  :garden {:builds [{:id           "screen"
                     :source-paths ["src/clj"]
                     :stylesheet   flashcards.css/screen
                     :compiler     {:output-to     "resources/public/css/screen.css"
                                    :pretty-print? true}}]}

  :shadow-cljs {:nrepl {:port 8777}
                
                :builds {:app {:target :browser
                               :output-dir "resources/public/js/compiled"
                               :asset-path "/js/compiled"
                               :modules {:app {:init-fn flashcards.core/init
                                               :preloads [devtools.preload]}}

                               :devtools {:http-root "resources/public"
                                          :http-port 8280
                                          :http-handler flashcards.handler/dev-handler
                                          }}}}
  
  :shell {:commands {"karma" {:windows         ["cmd" "/c" "karma"]
                              :default-command "karma"}
                     "open"  {:windows         ["cmd" "/c" "start"]
                              :macosx          "open"
                              :linux           "xdg-open"}}}

  :aliases {"dev"          ["do" 
                            ["shell" "echo" "\"DEPRECATED: Please use lein watch instead.\""]
                            ["watch"]]
            "watch"        ["with-profile" "dev" "do"
                            ["shadow" "watch" "app" "browser-test" "karma-test"]]

            "prod"         ["do"
                            ["shell" "echo" "\"DEPRECATED: Please use lein release instead.\""]
                            ["release"]]

            "release"      ["with-profile" "prod" "do"
                            ["shadow" "release" "app"]]

            "build-report" ["with-profile" "prod" "do"
                            ["shadow" "run" "shadow.cljs.build-report" "app" "target/build-report.html"]
                            ["shell" "open" "target/build-report.html"]]

            "karma"        ["do"
                            ["shell" "echo" "\"DEPRECATED: Please use lein ci instead.\""]
                            ["ci"]]
            "ci"           ["with-profile" "prod" "do"
                            ["shadow" "compile" "karma-test"]
                            ["shell" "karma" "start" "--single-run" "--reporters" "junit,dots"]]}

  :profiles {:dev {:dependencies [[binaryage/devtools "1.0.2"]]
                   :source-paths ["dev"]}

             :prod {}
   
             :uberjar {:source-paths ["env/prod/clj"]
                       :omit-source  true
                       :main         flashcards.server
                       :aot          [flashcards.server]
                       :uberjar-name "flashcards.jar"
                       :prep-tasks   ["compile" ["prod"]["garden" "once"]]}}

  :repl-options {:init (do (require '[flashcards.server :as server])
                           (def server (server/-main)))}

  :prep-tasks [["garden" "once"]])
