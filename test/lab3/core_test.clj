(ns lab3.core-test
  (:require [clojure.test :refer :all]
            [lab3.interpolation :refer :all]))

(deftest test-interpolate-by-linear
  (let [points [(->Point 1 2) (->Point 3 4)]]
    (testing "Линейная интерполяция между двумя точками"
      (is (= (linear-interpolation points 2) 3))
      (is (= (linear-interpolation points 1) 2))
      (is (= (linear-interpolation points 3) 4)))))

(deftest test-lagrange-coefficient
  (testing "Lagrange coefficient calculation"
    (let [points [(->Point 1 2) (->Point 2 3) (->Point 3 5)]]
      ;; Проверяем, что коэффициент соответствует ожидаемому значению
      (is (== (lagrange-basis-calculation points 1 2.5) 0.75)))

    (testing "Zero coefficient when x equals the point's x"
      (let [points [(->Point 0 1) (->Point 1 3) (->Point 2 4)]]
        ;; Если x совпадает с x_i, коэффициент должен быть равен 1
        (is (= (lagrange-basis-calculation points 0 0) 1))))

    (testing "Zero coefficient when x is unrelated"
      (let [points [(->Point 0 1) (->Point 1 3) (->Point 2 4)]]
        ;; Проверяем, что функция возвращает корректное значение
        (is (= (lagrange-basis-calculation points 1 0) 0))))))

(deftest test-interpolate-by-lagrange
  (let [points [(->Point 1 1) (->Point 2 4) (->Point 3 9)]]
    (testing "Интерполяция методом Лагранжа"
      (is (= (lagrange-interpolation points 2) 4))
      (is (= (lagrange-interpolation points 1.5) 2.25))
      (is (= (lagrange-interpolation points 2.5) 6.25)))))

(deftest test-generate-steps
  (testing "Генерация последовательности шагов"
    (is (= (generate-steps 1 5 1) [1 2 3 4 5]))
    (is (= (generate-steps 0 2.5 0.5) [0 0.5 1.0 1.5 2.0 2.5]))
    (is (= (generate-steps -1 1 0.5) [-1 -0.5 0.0 0.5 1.0 1.5]))))

(deftest test-interpolate-range
  (let [points [(->Point 1 1) (->Point 2 4) (->Point 3 9) (->Point 4 16)]]
    (testing "Интерполяция диапазона с линейным методом"
      (is (= (interpolate points 1 2 linear-interpolation)
             [(->Point 3 9) (->Point 4 16)])))
    (testing "Интерполяция диапазона с методом Лагранжа"
      (is (= (interpolate points 1 3 lagrange-interpolation)
             [(->Point 2 4) (->Point 3 9) (->Point 4 16)])))))

(run-tests)
