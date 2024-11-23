(ns lab3.input
  (:require [clojure.string :as str]))

(defn input [message parser]
  (println (str message ":"))
  (let [input (read-line)]
    (if (str/blank? input)
      (do (println "Empty input. Exiting.") (System/exit 0))
      (let [trimmed-input (str/trim input)
            split-input (str/split trimmed-input #"\s+")]
        (if (not= (count split-input) 2)
          (do
            (println "Please provide exactly two values separated by a space.")
            (input message parser))
          (try
            (parser trimmed-input)
            (catch Exception _
              (println "Incorrect input")
              (input message parser))))))))