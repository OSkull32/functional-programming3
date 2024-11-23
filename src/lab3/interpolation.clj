(ns lab3.interpolation)

(defrecord Point [x y])

(defn linear-interpolation
  [points x]
  (let [[p0 p1] (take-last 2 points)
        dy (- (:y p1) (:y p0))          ;; Разница по оси Y
        dx (- (:x p1) (:x p0))          ;; Разница по оси X
        t (/ (- x (:x p0)) dx)          ;; Нормализованное расстояние вдоль X
        interpolated-y (+ (* dy t) (:y p0))]
    interpolated-y))

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
             basis (lagrange-basis-calculation points i x)] ;; Базисная функция Лагранжа для точки i
         (+ result (* yi basis)))) ;; Добавляем вклад текущей точки
     0 ;; Начальное значение для накопителя
     (range n)))) ;; Перебираем все точки

(defn generate-steps [x-min x-max step]
  ;; Генерирует последовательность значений от x-min до x-max с шагом step
  (let [sequence (take-while #(<= % x-max) (iterate #(+ % step) x-min)) ;; Генерация последовательности
        last-value (+ (last sequence) step)] ;; Следующее значение, которое выходит за x-max
    (if (= (last sequence) x-max) ;; Если последнее значение в последовательности равно x-max
      (vec sequence) ;; Возвращаем последовательность без изменений
      (conj (vec sequence) last-value)))) ;; Добавляем следующее значение, если не достигнут x-max

(defn interpolate [points step window-size interpolate]
  ;; Интерполирует значения на основе последних window-size точек с шагом step
  (let [window (take-last window-size points) ;; Получаем последние window-size точек
        x-min (:x (first window)) ;; x-координата первой точки окна
        x-max (:x (last window)) ;; x-координата последней точки окна
        steps (generate-steps x-min x-max step)] ;; Генерируем шаги от x-min до x-max
    ;; Применяем интерполяцию для каждого значения x
    (mapv (fn [x] (->Point x (interpolate window x))) steps))) ;; Возвращаем вектор с интерполированными точками
