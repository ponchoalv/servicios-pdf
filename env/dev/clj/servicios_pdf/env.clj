(ns servicios-pdf.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [servicios-pdf.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[servicios_pdf started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[servicios_pdf has shut down successfully]=-"))
   :middleware wrap-dev})
