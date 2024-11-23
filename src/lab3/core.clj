(ns lab3.core
  (:require [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]
            [lab3.input :refer [input]]
            [lab3.interpolation :refer [linear-interpolation lagrange-interpolation interpolate]])
  (:import (lab3.interpolation Point)))

(def algorithms
  {"linear"
   {:interpolate linear-interpolation
    :window-size 2}
   "lagrange"
   {:interpolate lagrange-interpolation
    :window-size 4}})

(def cli-options
  [["-a" "--alg NAME" "Interpolation algorithm name (linear, lagrange)"
    :id :algorithms
    :multi true
    :default []
    :update-fn #(conj %1 (str/lower-case %2))
    :validate [#(contains? algorithms %) "Unknown algorithm name"]]
   ["-s" "--step STEP" "Step size"
    :id :step
    :default 1.0
    :validate [#(pos? %) "Step must be > 0"]
    :parse-fn #(Double/parseDouble %)]

   ["-h" "--help" "Help"]])

(def points (ref []))

(defn- usage [options-summary]
  (->> ["Laboratory work #3: Interpolation"
        ""
        "Usage: lab3.core [options]"
        ""
        "Options:"
        options-summary]
       (str/join \newline)))

(defn- exit [status msg]
  (println msg)
  (System/exit status))

(defn validate-args [args]
  (let [{:keys [options _ errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options)
      (exit 0 (usage summary))
      errors
      (exit 1 (str "The following errors occurred while parsing your command:\n"
                   (str/join \newline errors)))
      (empty? (:algorithms options))
      (exit 1 "Enter the algorithm name. Enter key clj -M -m lab3.core -h to help")
      :else
      {:options options})))

(defn- get-window-size [algorithm-name]
  ;; Получает размер окна для одного алгоритма
  (get-in algorithms [algorithm-name :window-size]))

(defn- max-window-size [algorithm-names]
  ;; Находит максимальный размер окна среди указанных алгоритмов
  (let [window-sizes (map get-window-size algorithm-names)] ;; Получаем все размеры окон
    (apply max window-sizes))) ;; Находим максимальный размер

(defn- parse-point [input]
  (let [numbers (str/split input #" ")]
    (if (= (count numbers) 2)
      (let [[x y] (map #(Double/parseDouble %) numbers)]
        (Point. x y))
      (throw (IllegalArgumentException.)))))

(defn- request-point [max-window-size]
  (dosync
   (alter points conj (input "Point input (x y separated by a space)" parse-point))
   (when (> (count @points) max-window-size)
     (alter points #(vec (rest %))))))

(defn- print-values [key result]
  ;; Печатает значения, отформатированные с двумя знаками после запятой, разделённые табуляцией
  (let [formatted-values (map #(format "%.2f" (key %)) result)] ;; Форматируем каждое значение
    (println (clojure.string/join "\t" formatted-values)))) ;; Выводим значения через табуляцию

(defn- print-interpolation-result [algorithm-name result]
  ;; Печатает результат интерполяции для указанного алгоритма
  (println (str "Result:"))
  (println (str (str/capitalize algorithm-name) " interpolation:"))
  (print-values :x result)
  (print-values :y result))

(defn- process-algorithm [algorithm-name options]
  ;; Обрабатывает интерполяцию для одного алгоритма
  (let [window-size (get-in algorithms [algorithm-name :window-size])]
    (when (>= (count @points) window-size) ;; Если достаточно точек для интерполяции
      (let [result (interpolate @points (:step options) window-size (get-in algorithms [algorithm-name :interpolate]))]
        (print-interpolation-result algorithm-name result)))))

(defn -main [& args]
  (let [{:keys [options exit-message]} (validate-args args)]
    (if exit-message
      (println exit-message)
      (let [max-window-size (max-window-size (:algorithms options))]
        (loop []
          (request-point max-window-size) ;; Запрашиваем точку
          (doseq [algorithm-name (:algorithms options)]
            (process-algorithm algorithm-name options)) ;; Обрабатываем каждый алгоритм
          (recur)))))) ;; Цикл продолжается
