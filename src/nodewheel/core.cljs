(ns nodewheel.core
  (:require [cljs.nodejs :as nodejs]
            [eulalie.core :as ec]
            [eulalie.lambda]
            [eulalie.lambda.util :as eu]
            [cljs.core.async :as a]
            [cljs.reader :as edn])
  (:require-macros [cljs.core.async.macros :as a]))

(def jszip (js/require "jszip"))
(def fs (js/require "fs"))

(nodejs/enable-util-print!)

(defn lambda-handler [event ctx callback]
  (let [{:keys [randomly-named-keys]} (js->clj event :keywordize-keys true)]
    (prn event)
    (prn randomly-named-keys)
    (callback nil (clj->js {:hello "hello"}))))

(set! (.-exports js/module) (clj->js {:handler lambda-handler}))

(defn read-file
  ([path]
    (read-file path identity))
  ([path transform-fn]
   (let [ch (a/promise-chan (map transform-fn))]
     (.readFile fs path (fn [err data]
                          (if err
                            (throw err)
                            (a/put! ch data))))
     ch)))

(defn recursively-zip-dirs
  ([root-path]
    (recursively-zip-dirs (jszip.) root-path))
  ([zip root-path]
    (a/go-loop [files (.readdirSync fs root-path)
                zip zip]
      (if-let [file-name (first files)]
        (let [file-path (str root-path "/" file-name)
              stat (.statSync fs file-path)]
          (cond
            (.isFile stat) (recur
                             (rest files)
                             (.file zip file-path (a/<! (read-file file-path))))
            (.isDirectory stat) (recur (concat (rest files)
                                               (map #(str file-name "/" %1)
                                                    (.readdirSync fs file-path)))
                                       zip)
            :default (recur (rest files)
                            zip)))
        zip))))

(defn zip-file
  ([path]
    (zip-file (jszip.) path))
  ([zip path]
    (zip-file zip path path))
  ([zip path zip-path]
   (a/go (let [buf (a/<! (read-file path))]
           (.file zip zip-path buf)))))

(defn zip-lambda-fn [file-name]
  (let [result-ch (a/promise-chan)]
    (a/go (cond-> (zip-file file-name)
                  true (a/<!)
                  (.existsSync fs "node_modules") (recursively-zip-dirs "node_modules")
                  true (a/<!)
                  true (.generateAsync (clj->js {:type "base64"}))
                  true (.then #(a/put! result-ch %1))))
    result-ch))

(def lambda-name "a-function")
(defn get-creds []
  (a/go (a/<! (read-file "creds.edn" #(edn/read-string (.toString %1))))))

(defn -main [& args]
  (when-let [zip-target (first args)]
    (a/go
      (let [creds (a/<! (get-creds))]
        (when-not (instance? ExceptionInfo (eu/get-function! creds lambda-name))
          (println "Deleting existing function...")
          (a/<! (eu/delete-function! creds lambda-name))
          (println "Deleted function."))

        (println "Zipping and encoding...")
        (let [encoded-zip (a/<! (zip-lambda-fn zip-target))]
          (println "Zipped function. Uploading...")
          (a/<! (ec/issue-request! {:service :lambda
                                    :target :create-function
                                    :creds creds
                                    :body {:code {:zip-file encoded-zip}
                                           :function-name lambda-name
                                           :handler "nodewheelDeployable.handler"
                                           :role (:role creds)
                                           :runtime "nodejs4.3"}}))
          (println "Uploaded function")
          (println "Making request")
          (-> (eu/request! creds lambda-name {:randomly-named-keys 1})
              (a/<!)
              (prn))
          (println "Call complete"))))))

(set! *main-cli-fn* -main)
