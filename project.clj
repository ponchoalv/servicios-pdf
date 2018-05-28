(defproject servicios-pdf "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[clj-time "0.14.3"]
                 [compojure "1.6.1"]
                 [cprop "0.1.11"]
                 [funcool/struct "1.2.0"]
                 [luminus-immutant "0.2.4"]
                 [luminus-nrepl "0.1.4"]
                 [luminus/ring-ttl-session "0.3.2"]
                 [markdown-clj "1.0.2"]
                 [metosin/compojure-api "1.1.11"]
                 [metosin/muuntaja "0.5.0"]
                 [metosin/ring-http-response "0.9.0"]
                 [mount "0.1.12"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.cli "0.3.6"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.webjars.bower/tether "1.4.3"]
                 [org.webjars/bootstrap "4.1.0"]
                 [org.webjars/font-awesome "5.0.10"]
                 [org.webjars/jquery "3.2.1"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.7.0-RC1"]
                 [ring/ring-defaults "0.3.1"]
                 [selmer "1.11.7"]
                 [net.sf.jasperreports/jasperreports "6.5.1"]
                 [com.documents4j/documents4j-local "1.0.3"]
                 [com.documents4j/documents4j-client "1.0.3"]
                 [com.documents4j/documents4j-aggregation "1.0.3"]
                 [com.documents4j/documents4j-transformer-msoffice-word "1.0.3"]
                 [fr.opensagres.xdocreport/fr.opensagres.xdocreport.document.docx "2.0.1"]
                 [fr.opensagres.xdocreport/fr.opensagres.xdocreport.template.velocity "2.0.1"]]

  :repositories [["itext" "https://jaspersoft.artifactoryonline.com/jaspersoft/third-party-ce-artifacts/"]]
  :min-lein-version "2.0.0"

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ^:skip-aot servicios-pdf.core

  :plugins [[lein-immutant "2.1.0"]]

  :profiles
  {:uberjar       {:omit-source    true
                   :aot            :all
                   :uberjar-name   "servicios-pdf.jar"
                   :source-paths   ["env/prod/clj"]
                   :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev   {:jvm-opts       ["-Dconf=dev-config.edn"]
                   :dependencies   [[expound "0.5.0"]
                                    [pjstadig/humane-test-output "0.8.3"]
                                    [prone "1.5.2"]
                                    [ring/ring-devel "1.6.3"]
                                    [ring/ring-mock "0.3.2"]]
                   :plugins        [[com.jakemccrary/lein-test-refresh "0.19.0"]]

                   :source-paths   ["env/dev/clj"]
                   :resource-paths ["env/dev/resources"]
                   :repl-options   {:init-ns user}
                   :injections     [(require 'pjstadig.humane-test-output)
                                    (pjstadig.humane-test-output/activate!)]}
   :project/test  {:jvm-opts       ["-Dconf=test-config.edn"]
                   :resource-paths ["env/test/resources"]}
   :profiles/dev  {}
   :profiles/test {}})
