(ns ray-trace-2d.soln-gen
  (:require [ray-trace-2d.structs :as structs])
  (:use [clojure.core.matrix]
        [clojure.core.matrix.operators]))

(defn euclidean-dist [p1 p2]
  (length (- p2 p1)))

(comment

(defn normal-cw [dir]
  (structs/make-raw-direction
    (structs/get-y dir)
    (- (structs/get-x dir))))

(defn normal-cc [dir]
  (structs/make-raw-direction
    (- (structs/get-y dir))
    (structs/get-x dir)))

)

(defn normal-rot [dir]
  (structs/rotate-2d dir (structs/make-point 0.0 0.0) 90.0))

(defn reflect [light mirror]
  (let [d0 (light :direction)
        d1 (structs/make-normal-direction (mirror :p1) (mirror :p2))
        n1 (normal-rot d1)]
    (- d0 (* 2 (dot n1 d0) n1))))

;; needs testing
(defn light-mirror-hit [light mirror]
  (let [a0 (light :origin)
        an (light :direction)
        b0 (mirror :p1)
        b1 (mirror :p2)
        bl (length (- b1 b0))
        bn (structs/make-normal-direction b0 b1)
        get-x structs/get-x
        get-y structs/get-y]
    ;; parallel test
    (if-not (zero? (double (dot (normal-rot bn) an)))
      ;; intersect test
      (let [s-num (-
                    (* (- (get-y b0) (get-y a0)) (get-x bn))
                    (* (- (get-x b0) (get-x a0)) (get-y bn)))
            t-num (-
                    (* (get-x an) (- (get-y b0) (get-y a0)))
                    (* (get-y an) (- (get-x b0) (get-x a0))))
            st-den (- (* (get-y an) (get-x bn)) (* (get-x an) (get-y bn)))
            s (try (/ s-num st-den) (catch Exception e (println "0 Div")) (finally -1))
            t (try (/ t-num st-den) (catch Exception e (println "0 Div")) (finally -1))
            hit (+ a0 (* s an))
            hit-dist (euclidean-dist a0 hit)]
        (if (and (>= s 0) (<= 0 t) (<= t bl) (>= hit-dist 0.1))
          {:intersect 1
            :at hit
            :with mirror
            :distance hit-dist}
          {:intersect 0}))
      ;; overlap test
      (let [a0-b0-dist (euclidean-dist a0 b0)
            a0-b1-dist (euclidean-dist a0 b1)
            closer (if (< a0-b0-dist a0-b1-dist) b0 b1)
            closer-dist (euclidean-dist a0 closer)
            a0-closer-dir (structs/make-normal-direction a0 closer)]
        (if (== a0-closer-dir an)
          {:intersect 2
            :at closer
            :with mirror
            :distance closer-dist}
          {:intersect 0})))))

(defn solution-step [problem solution]
  (let [mirrors (problem :mirrors)
        reflections (solution :reflections)
        last-reflection (structs/make-light (last reflections) (solution :last-dir))
        hit-by-light (filter (fn [result]
                        (or (= 1 (result :intersect)) (= 2 (result :intersect))))
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
            (if (= 2 (closest :intersect))
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