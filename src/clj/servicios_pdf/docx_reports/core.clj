(ns servicios-pdf.docx-reports.core
  (:require [servicios-pdf.utils.core :refer [merge-doc map->java-map pdf-piped-resp]]
            [ring.util.io :refer [piped-input-stream]]
            [ring.util.http-response :refer [ok]]
            [servicios-pdf.converters.core :refer [converter-list]])
  (:import [com.documents4j.api DocumentType]))

(defn- prepare-gen-data [req-data]
  (map->java-map (:template-data req-data)))

(defn- fill-template [req-data]
  (piped-input-stream
    (fn [filled]
      (merge-doc (:template-name req-data) filled (prepare-gen-data req-data)))))

(defn- pdf-from-template [req-data]
  (piped-input-stream
    (fn [out]
      @(-> (:aggregated-converter @converter-list)
           (.convert (fill-template req-data))
           (.as DocumentType/MS_WORD)
           (.to out)
           (.as DocumentType/PDF)
           (.schedule)))))

(defn- pdf-from-uploaded-file [file]
  (piped-input-stream
    (fn [^java.io.ByteArrayOutputStream output]
      @(-> (:aggregated-converter @converter-list)
           (.convert file)
           (.as DocumentType/MS_WORD)
           (.to output)
           (.as DocumentType/PDF)
           (.schedule)))))

(defn gen-pdf-resp [template-data]
  (try
    (pdf-piped-resp (pdf-from-template template-data) (:output-file template-data))
    (catch Exception ex
      (ok {:error (.getMessage ex)}))))

(defn to-pdf [file]
  (try
    (pdf-piped-resp (pdf-from-uploaded-file (:tempfile file)) "reporte.pdf")
    (catch Exception ex
      (ok {:error (.getMessage ex)}))))