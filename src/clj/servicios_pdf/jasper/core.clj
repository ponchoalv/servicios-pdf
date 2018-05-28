(ns servicios-pdf.jasper.core
  (:require [clojure.java.io :as io]
            [ring.util.response :as resp]
            [ring.util.http-response :refer :all]
            [clojure.core.reducers :as r]
            [ring.util.io :refer [piped-input-stream]]
            [servicios-pdf.config :refer [env]]
            [servicios-pdf.utils.core :refer [pdf-piped-resp]]
            [clojure.tools.logging :as log])
  (:import [net.sf.jasperreports.engine
            JasperFillManager
            JasperExportManager
            JasperPrint
            JasperCompileManager
            JREmptyDataSource]
           [net.sf.jasperreports.engine.data JRMapCollectionDataSource]
           [java.util ArrayList]
           (com.lowagie.text.pdf PdfReader)
           (com.lowagie.text.pdf PdfStamper)
           (java.io FileOutputStream File)
           (com.lowagie.text.pdf PdfFormField)
           (com.lowagie.text Rectangle)
           (com.lowagie.text.pdf PdfAnnotation PdfSignatureAppearance)
           [org.bouncycastle.jce.provider BouncyCastleProvider]
           [java.security KeyStore Security]
           [java.security.cert CertStore CollectionCertStoreParameters]
           [java.util ArrayList]))

(defn- java-hashmap [results-map]
  (let [hashmap-reducer (fn [m k v] (doto m (.put (name k) v)))]
    (r/reduce hashmap-reducer (java.util.HashMap.) results-map)))

(defn- get-jr-map [java-map-list]
  (let [reducer (fn [acc el] (do (.add acc el) acc))
        array-list (r/reduce reducer (ArrayList.) java-map-list)]
    (JRMapCollectionDataSource. array-list)))

(defn- prepare-data [template-data]
  (let [data-source-items (get-jr-map (map java-hashmap (:ItemDataSource template-data)))]
    (java-hashmap (assoc-in template-data [:ItemDataSource] data-source-items))))

(defn- jasper-fill [report-data report-name]
  (JasperFillManager/fillReport
    (io/input-stream (io/file (str (env :jasper-templates) report-name)))
    report-data
    (JREmptyDataSource.)))

(defn- export-file
  "Ejemplo de como usar:
  (time (let [jr-data (get-jr-map (gen-data map-row java-hashmap))\n
             filled (jasper-fill (java-hashmap (servicios_pdf-data jr-data)))]\n
                    (JasperExportManager/exportReportToPdfFile filled \"result.pdf\")))"
  [jasper-fill filename]
  (JasperExportManager/exportReportToPdfFile jasper-fill filename))

(defn- stream-srt-pdf [template-data]
  (piped-input-stream
    (fn [out]
      (let [jr-data (prepare-data (:template-data template-data))
            filled (jasper-fill jr-data (:template-name template-data))]
        (JasperExportManager/exportReportToPdfStream filled out)))))

(defn jastper-to-pdf [data]
  (try
    (pdf-piped-resp (stream-srt-pdf data) (:output-file data))
    (catch Exception ex
      (ok {:error (.getMessage ex)}))))

(defn- add-signature-annotations [stamper field number-of-pages]
  (loop [x 0]
    (when (< x number-of-pages)
      (do (.addAnnotation stamper field (+ x 1))
          (recur (+ x 1))))))

(defn- add-signature-field
  [stamper field-data number-of-pages]
  (let [field (PdfFormField/createSignature (.getWriter stamper))]
    (.setFieldName field (:name field-data))
    (.setWidget field (Rectangle. (:de-x field-data) (:de-y field-data) (:a-x field-data) (:a-y field-data)) PdfAnnotation/HIGHLIGHT_OUTLINE)
    (.setFlags field PdfAnnotation/FLAGS_PRINT)
    (add-signature-annotations stamper field number-of-pages)))

(defn- add-signature-field-to-all-pages
  "Agregar campo para firmas en todas las paginas en la misma posición."
  [file field-data]
  (piped-input-stream
    (fn [out]
      (let [src (io/input-stream (:tempfile file))
            reader (PdfReader. src)
            number-of-pages (.getNumberOfPages reader)
            stamper (PdfStamper. reader out)]
        (do (add-signature-field stamper field-data number-of-pages)
            (.close stamper))))))

(defn add-fields-to-all-pages [file field-data]
  (try
    (log/info field-data)
    (pdf-piped-resp (add-signature-field-to-all-pages file field-data) (:output-file (:filename file)))
    (catch Exception ex
      (ok {:error (.getMessage ex)}))))