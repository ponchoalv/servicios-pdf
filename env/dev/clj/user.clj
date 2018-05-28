(ns user
  (:require [servicios-pdf.config :refer [env]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [mount.core :as mount]
            [servicios-pdf.core :refer [start-app]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start []
  (mount/start-without #'servicios-pdf.core/repl-server))

(defn stop []
  (mount/stop-except #'servicios-pdf.core/repl-server))

(defn restart []
  (stop)
  (start))


