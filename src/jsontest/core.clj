(ns jsontest.core)

(require '[clj-serializer.core :as serializer])
(require '[clj-json.core :as m-json])
(require '[org.danlarkin.json :as dl-json])
(require '[clojure.contrib.json :as cc-json-read])
(require '[clojure.contrib.json :as cc-json-write])

(defn nano-time []
  (System/nanoTime))

(defn timed [task]
  (let [t-start (nano-time)
        res     (task)
        t-end   (nano-time)]
    (double (/ (- t-end t-start) 1000000000))))

(def record
  {:keyword :foo
   :string "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
   :integer 3
   :long 52001110638799097
   :double 1.23
   :boolean true
   :nil nil
   :map {"hi" 9 "low" 0}
   :vector ["a" "b" "c"]
   :list '(1 2 3)})

(def roundtrips 100000)

(defn bench [name f]
  (print name "  ")
  (flush)
  (printf "%.2f\n" (timed #(dotimes [_ roundtrips] (f record))))
  (flush))

(defn testit
  []
  (println "Clojure version: "
           (str (:major       *clojure-version*) "."
                (:minor       *clojure-version*) "."
                (:incremental *clojure-version*)))
  (println "Num roundtrips:  "  roundtrips)
  (println)
  (dotimes [i 2]
    (println "Trail: " (inc i))
    (bench "clj-json                            "
           #(m-json/parse-string (m-json/generate-string %)))
    (bench "clj-json-smile                      "
           #(m-json/parse-smile (m-json/generate-smile %)))
    (bench "clj-json w/ keywords                "
           #(reduce (fn [m [k v]] (assoc m (keyword k) v)) {}
                    (m-json/parse-string (m-json/generate-string %))))
    (bench "clj-serializer                      "
           #(serializer/deserialize (serializer/serialize %) :eof))
    (bench "clojure printer/reader              "
           #(read-string (prn-str %)))
    (bench "clojure printer/reader w/ print-dup "
           #(binding [*print-dup* true] (read-string (prn-str %))))
    (bench "org.danlarkin.json                  "
           #(dl-json/decode-from-str (dl-json/encode-to-str %)))
    (bench "clojure.contrib.json                "
           #(cc-json-read/read-json (cc-json-write/json-str %)))
    ;; (bench "clojure.contrib.json w/ keywords    "
    ;;        #(binding [cc-json-read/*json-keyword-keys* true]
    ;;           (cc-json-read/read-json (cc-json-write/json-str %))))
    (println))

  (println "Size comparison:")
  (println "clj-json       " (count (m-json/generate-string record)))
  (println "clj-json-smile " (count (m-json/generate-smile record))))

