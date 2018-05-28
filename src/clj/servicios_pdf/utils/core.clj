(ns servicios-pdf.utils.core
  (:require [clojure.core.reducers :as r]
            [servicios-pdf.config :refer [env]]
            [ring.util.response :as resp]
            [ring.util.http-response :refer [header content-type]])
  (:import [fr.opensagres.xdocreport.document IXDocReport]
           [fr.opensagres.xdocreport.document.registry XDocReportRegistry]
           [fr.opensagres.xdocreport.template TemplateEngineKind]))

(defn java-hashmap [results-map]
  (let [hashmap-reducer (fn [m k v] (doto m (.put (name k) v)))]
    (r/reduce hashmap-reducer (java.util.HashMap.) results-map)))

(defn list-of-maps->java [coll]
  (java.util.ArrayList.
    (map #(java.util.HashMap. %) coll)))

(defn map->java-map [results-map]
  (into {}
        (for [[k v] results-map]
          (if (vector? v) [(name k) (list-of-maps->java (map map->java-map v))]
                          [(name k) v]))))

(defn merge-doc-file
  "Genera un word a partir de un template y la información a completar. Funciona solo con templates en formato .docx."
  ([input-file output-file columns context-map]
   (let [in (clojure.java.io/input-stream (clojure.java.io/file (str (env :template-folder) input-file)))
         out (clojure.java.io/output-stream output-file)
         report (. (XDocReportRegistry/getRegistry) loadReport in TemplateEngineKind/Velocity)
         fields-metadata (.createFieldsMetadata report)
         context (. report createContext)]
     (doseq [col columns]
       (.addFieldAsList fields-metadata col))
     (.setFieldsMetadata report fields-metadata)
     (doseq [[k v] context-map]
       (if (seq? v)
         (. context (put (name k) (list-of-maps->java v)))
         (. context (put (name k) v))))
     (. report process context out)))
  ([input-file output-file context-map]
   (merge-doc-file input-file output-file [] context-map)))

(defn merge-doc
  "Genera un word a partir de un template y la información a completar. Funciona solo con templates en formato .docx."
  ([input-file out columns context-map]
   (let [in (clojure.java.io/input-stream (clojure.java.io/file (str (env :template-folder) input-file)))
         report (. (XDocReportRegistry/getRegistry) loadReport in TemplateEngineKind/Velocity)
         fields-metadata (.createFieldsMetadata report)
         context (. report createContext)]
     (doseq [col columns]
       (.addFieldAsList fields-metadata col))
     (.setFieldsMetadata report fields-metadata)
     (doseq [[k v] context-map]
       (if (seq? v)
         (. context (put (name k) (list-of-maps->java v)))
         (. context (put (name k) v))))
     (. report process context out)))
  ([input-file output-file context-map]
   (merge-doc input-file output-file [] context-map)))

(defn pdf-piped-resp [piped filename]
  (-> (resp/response piped)
      (header "Content-Disposition" (str "filename=" filename))
      (content-type "application/pdf")))