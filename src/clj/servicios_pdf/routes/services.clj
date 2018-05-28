(ns servicios-pdf.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [servicios-pdf.jasper.core :as j]
            [servicios-pdf.docx-reports.core :as r]
            [clojure.tools.logging :as log]
            [servicios-pdf.converters.core :refer [add-converter remove-converter converter-list gen-result gen-result-row]]
            [ring.swagger.upload :refer [TempFileUpload]]))

(s/def GenericTemplate {:template-name s/Str
                        :output-file   s/Str
                        :template-data s/Any})

(s/def ConverterRequest {:converter-name s/Str
                         :converter-uri  s/Str})


(defapi service-routes
  {:swagger {:ui   "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version     "1.0.0"
                           :title       "PDF Manager"
                           :description "API para generación de PDFs."}}}}

  (context "/api" []
    :tags ["servicios_pdf-manager"]

    (POST "/pdf-from-jasper-template" []
      :produces ["application/pdf"]
      :body [template-data GenericTemplate]
      :summary "Genera PDF desde un template jasper. Equivalencia 1 a 1 con los campos dentro del jasper.
      datos obligatorios:template-name (archivo .jasper), ItemDataSource (si no hay lista de datos debe ir con una lista vacia []"
      (j/jastper-to-pdf template-data))

    (POST "/add-fields-on-all-pages" []
      :produces ["application/pdf"]
      :query-params [de-x :- Long
                     de-y :- Long
                     a-x :- Long
                     a-y :- Long
                     name :- String]
      :multipart-params [file :- TempFileUpload]
      :summary "Agrega campo para firmas en ubicación (X;Y) con tamaño WidthxHeigth (en pixeles ambos datos)"
      (j/add-fields-to-all-pages file {:de-x de-x
                                       :de-y de-y
                                       :a-x a-x
                                       :a-y a-y
                                       :name name}))

    (POST "/pdf-from-word-template" []
      :produces ["application/pdf"]
      :body [req-data GenericTemplate]
      :summary "Genera PDF desde template MS Word y Velocity. Tomando un set de datos generico."
      (r/gen-pdf-resp req-data))

    (POST "/pfd-from-word-upload" []
      :produces ["application/pdf"]
      :multipart-params [file :- TempFileUpload]
      :summary "Genera PDF desde un archivo word .doc o .docx. El archivo .doc se debe subir usando un formulario multipart.
      Devuelve un PDF con header application/pdf"
      (r/to-pdf file))

    (GET "/converter-list" []
      :return s/Any
      :summary "Informa la lista de conversores PDF cargadas en el pool"
      (ok (gen-result gen-result-row converter-list)))

    (PUT "/add-converter" []
      :body [convReq ConverterRequest]
      :summary "Agregar un conversor de PDF"
      :return Boolean
      (ok (do
            (add-converter converter-list (:converter-name convReq) (:converter-uri convReq))
            true)))

    (DELETE "/remove-converter" []
      :body [converter-name s/Str]
      :summary "Eliminar un conversor de PDF"
      :return Boolean
      (ok (do
            (remove-converter converter-list converter-name)
            true)))))
