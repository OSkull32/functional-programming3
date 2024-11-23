# Лабораторная работа №3

---

**Выполнил:** Данченко Владимир Витальевич, 368087

**Группа:** P3334

---

# Цель:

Получить навыки работы с вводом/выводом, потоковой обработкой данных, командной строкой.

---

## Требования к разработанному ПО:

1. обязательно должна быть реализована линейная интерполяция (отрезками, link);
2. настройки алгоритма интерполяции и выводимых данных должны задаваться через аргументы командной строки:
   - какие алгоритмы использовать (в том числе два сразу);
   - частота дискретизации результирующих данных;
   - и т.п.;

3. входные данные должны задаваться в текстовом формате на подобии ".csv" (к примеру x;y\n или x\ty\n) и подаваться на стандартный ввод, входные данные должны быть отсортированы по возрастанию x;
4. выходные данные должны подаваться на стандартный вывод;
5. программа должна работать в потоковом режиме (пример -- cat | grep 11), это значит, что при запуске программы она должна ожидать получения данных на стандартный ввод, и, по мере получения достаточного количества данных, должна выводить рассчитанные точки в стандартный вывод;

## Реализация

### Функция запроса ввода пользователя

Запрашивает у пользователя ввод, обрабатывает его и парсит в соответствии с переданным парсером.

```clojure
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
```
### Функция для обработки элементов командной строки
Предназначена для описания и обработки параметров командной строки в приложении.
```clojure
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
```

### Функция для создания равномерной числовой последовательности, используемой в вычислениях

```clojure
(defn generate-steps [x-min x-max step]
   ;; Генерирует последовательность значений от x-min до x-max с шагом step
   (let [sequence (take-while #(<= % x-max) (iterate #(+ % step) x-min)) ;; Генерация последовательности
         last-value (+ (last sequence) step)] ;; Следующее значение, которое выходит за x-max
      (if (= (last sequence) x-max) ;; Если последнее значение в последовательности равно x-max
         (vec sequence) ;; Возвращаем последовательность без изменений
         (conj (vec sequence) last-value)))) ;; Добавляем следующее значение, если не достигнут x-max
```
Генерирует последовательность чисел от x-min до x-max с заданным шагом step.
## Реализация алгоритмов интерполяции

---

### Линейная интерполяция

```clojure
(defn linear-interpolation
   [points x]
   (let [[p0 p1] (take-last 2 points)
         dy (- (:y p1) (:y p0))          ;; Разница по оси Y
         dx (- (:x p1) (:x p0))          ;; Разница по оси X
         t (/ (- x (:x p0)) dx)          ;; Нормализованное расстояние вдоль X
         interpolated-y (+ (* dy t) (:y p0))]
      interpolated-y))
```
Выполняет линейную интерполяцию для заданной точки x на основе двух известных точек.
### Интерполяция Лагранжа

```clojure
(defn lagrange-basis-calculation
   [points i x]
   ;; Вычисляет i-ю базисную функцию Лагранжа для заданного x
   (let [n (count points) ;; Количество точек
         xi (:x (nth points i))] ;; x-координата текущей точки i
      (reduce
         (fn [acc j]
            (if (= i j)
               acc ;; Пропускаем, если i равно j
               (let [xj (:x (nth points j)) ;; x-координата точки j
                     term (/ (- x xj) (- xi xj))] ;; Один из множителей
                  (* acc term)))) ;; Умножаем накопитель на множитель
         1
         (range n)))) ;; Перебираем все точки

(defn lagrange-interpolation [points x]
   ;; Интерполяция Лагранжа для заданных точек и значения x
   (let [n (count points)] ;; Количество точек
      (reduce
         (fn [result i]
            (let [yi (:y (nth points i)) ;; y-координата текущей точки
                  basis (lagrange-basis points i x)] ;; Базисная функция Лагранжа для точки i
               (+ result (* yi basis)))) ;; Добавляем вклад текущей точки
         0 ;; Начальное значение для накопителя
         (range n)))) ;; Перебираем все точки
```
Функция lagrange-basis-calculation вычисляет i-ю базисную функцию Лагранжа для интерполяции в заданной точке x на основе набора точек.

Функция lagrange-interpolation выполняет интерполяцию Лагранжа для заданного набора точек и значения x.

## Ввод/вывод программы

--- 
Пример вычислений для шага 1.0 и функции sin(x):
```
Point input (x y separated by a space):
0 0
Point input (x y separated by a space):
1.571 1
Result:
Linear interpolation:
0,00    1,00    2,00
0,00    0,64    1,27
Point input (x y separated by a space):
3.142 0
Result:
Linear interpolation:
1,57    2,57    3,57
1,00    0,36    -0,27
Point input (x y separated by a space):
4.712 -1
Result:
Linear interpolation:
3,14    4,14    5,14
0,00    -0,64   -1,27
Result:
Lagrange interpolation:
0,00    1,00    2,00    3,00    4,00    5,00
0,00    0,97    0,84    0,12    -0,67   -1,03
Point input (x y separated by a space):
12.568 0
Result:
Linear interpolation:
4,71    5,71    6,71    7,71    8,71    9,71    10,71   11,71   12,71
-1,00   -0,87   -0,75   -0,62   -0,49   -0,36   -0,24   -0,11   0,02
Result:
Lagrange interpolation:
1,57    2,57    3,57    4,57    5,57    6,57    7,57    8,57    9,57    10,57   11,57   12,57
1,00    0,37    -0,28   -0,91   -1,49   -1,95   -2,26   -2,38   -2,25   -1,84   -1,11   0,00

```

Вывод программы полностью совпадает с выводом в описание лабораторной работы

## Вывод

---

В ходе выполнения лабораторной работы было реализовано приложение для выполнения интерполяции в режиме потоковой обработки данных.
Мной были освоены принципы потоковой обработки данных, работы с командной строкой и вводом/выводом.
Также я реализовал алгоритмы интерполяции на функциональном языке программирования


