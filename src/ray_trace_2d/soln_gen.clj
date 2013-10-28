(ns ray-trace-2d.soln-gen
  (:require [ray-trace-2d.structs :as structs]
            [ray-trace-2d.collision :as collision])
  (:use [clojure.core.matrix]
        [clojure.core.matrix.operators]))

(defn reflect [light mirror]
  (let [d0 (light :direction)
        d1 (structs/make-normal-direction (mirror :p1) (mirror :p2))
        n1 (structs/normal-rot d1)]
    (- d0 (* 2 (dot n1 d0) n1))))

(def light-mirror-hit collision/algebraic)

(defn solution-step [problem solution]
  (let [mirrors (problem :mirrors)
        reflections (solution :reflections)
        last-reflection (structs/make-light (last reflections) (solution :last-dir))
        hit-by-light (filter (fn [result]
                        (or (= collision/HIT (result :intersect)) (= collision/OVERLAP (result :intersect))))
                      (map (fn [mirror]
                        (light-mirror-hit last-reflection mirror)) mirrors))]
        (if
          (or
            (empty? hit-by-light)
            (and
              (zero? (structs/get-x (solution :last-dir)))
              (zero? (structs/get-y (solution :last-dir)))))
          solution ;; ignore corner cases (corner)
          (let [closest (first (sort-by :distance hit-by-light))
                mirror (closest :with)
                reflect-pt (closest :at)
                reflect-dir (reflect last-reflection mirror)]
            (if (= collision/OVERLAP (closest :intersect))
              (structs/extend-solution solution reflect-pt
                (structs/make-raw-direction 0.0 0.0)) ;; ignore parallel-edge cases
              (structs/extend-solution solution reflect-pt reflect-dir))))))

(defn iter-solution [problem solution n]
  (if (< n 0)
    solution
    (iter-solution problem (solution-step problem solution) (- n 1))))

(defn generate-solution [problem]
  "Generates a solution to input problem."
  (iter-solution problem (structs/make-solution problem) (problem :n)))