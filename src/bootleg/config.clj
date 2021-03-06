(ns bootleg.config
  (:require [bootleg.file :as file]
            [clojure.java.io :as io])
  (:import [java.util Base64]))

(def libs-set ["sunec.lib" "sunec.dll" "libsunec.dylib" "libsunec.so"])

(defn setup
  "Copy any of the bundled dynamic libs from resources to the
  run time lib directory"
  [libs-dir]
  (doseq [filename libs-set]
    (when-let [file (io/resource filename)]
      (let [[_ name] (file/path-split (.getFile file))]
        ;; (println "installing:" name)
        (io/copy (io/input-stream file) (io/file (file/path-join libs-dir name)))))))

(defn init! []
  (let [native-image?
        (and (= "Substrate VM" (System/getProperty "java.vm.name"))
             (= "runtime" (System/getProperty "org.graalvm.nativeimage.imagecode")))
        home-dir (System/getenv "HOME")
        config-dir (file/path-join home-dir ".bootleg")
        libs-dir (file/path-join config-dir "libs")]
    (.mkdirs (io/as-file libs-dir))

    (when native-image?
      ;; https://blog.taylorwood.io/2018/10/04/graalvm-https.html
      (setup libs-dir)
      (System/setProperty "java.library.path" libs-dir))))
