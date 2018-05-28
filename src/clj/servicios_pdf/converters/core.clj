(ns servicios-pdf.converters.core
  (:require [mount.core :as mount])
  (:import  (com.documents4j.job AggregatingConverter RemoteConverter)
           (com.documents4j.api IConverter)))

;; Agreggated MS Office Converter
(defn get-agreggated-converter [^IConverter remote-converter]
  (AggregatingConverter/make (into-array [remote-converter])))

(defn add-converter-atom [atom key converter-uri converter-value]
  (assoc atom key [converter-value converter-uri]))

(defn add-converter-first-time [atom key aggregated-converter]
  (assoc atom key aggregated-converter))

(defn add-converter [atom converter-key converter-uri]
  (let [key (keyword converter-key)
        value (RemoteConverter/make converter-uri)]
    (if-let [aggregated-converter (:aggregated-converter @atom)]
      (.register aggregated-converter value)
      (swap! atom add-converter-first-time :aggregated-converter (get-agreggated-converter value)))
    (swap! atom add-converter-atom key converter-uri value)))

(defn remove-converter-atom [atom key]
  (dissoc atom key))

(defn remove-converter [atom converter-key]
  (let [convert-key (keyword converter-key)]
    (do
      (.remove (:aggregated-converter @atom) (nth (convert-key @atom) 0))
      (swap! atom remove-converter-atom convert-key))))

(defn gen-result-row [m]
  {(nth m 0) (nth (nth m 1) 1)})

(defn gen-result [result-row-fn converter-list-atom]
  (map result-row-fn (dissoc @converter-list-atom :aggregated-converter)))

(mount/defstate converter-list
                :start
                (atom {})
                :stop
                (do (when-let [aggregater-converter (:aggregated-converter @converter-list)]
                      (.shutDown aggregater-converter))
                    (reset! converter-list {})))