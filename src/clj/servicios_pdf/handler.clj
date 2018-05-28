(ns servicios-pdf.handler
  (:require 
            [servicios-pdf.routes.services :refer [service-routes]]
            [compojure.core :refer [routes wrap-routes]]
            [compojure.route :as route]
            [servicios-pdf.env :refer [defaults]]
            [mount.core :as mount]
            [servicios-pdf.middleware :as middleware]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(mount/defstate app
  :start
  (middleware/wrap-base
    (routes
          #'service-routes
          (route/not-found
             "page not found"))))

