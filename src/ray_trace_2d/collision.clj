(ns ray-trace-2d.collision
  (:require [ray-trace-2d.structs :as structs])
  (:use [clojure.core.matrix]
        [clojure.core.matrix.operators]))

(def ^:const MISS 0)
(def ^:const HIT 1)
(def ^:const OVERLAP 2)

(defn algebraic [light mirror]
  (let [a0 (light :origin)
        an (light :direction)
        b0 (mirror :p1)
        b1 (mirror :p2)
        bl (length (- b1 b0))
        bn (structs/make-normal-direction b0 b1)
        get-x structs/get-x
        get-y structs/get-y]
    ;; parallel test
    (if-not (zero? (double (dot (structs/normal-rot bn) an)))
      ;; intersect test
      (let [s-num (-
                    (* (- (get-y b0) (get-y a0)) (get-x bn))
                    (* (- (get-x b0) (get-x a0)) (get-y bn)))
            t-num (-
                    (* (get-x an) (- (get-y b0) (get-y a0)))
                    (* (get-y an) (- (get-x b0) (get-x a0))))
            st-den (- (* (get-y an) (get-x bn)) (* (get-x an) (get-y bn)))
            s (if (== st-den 0)
                (if (== (get-x bn) 0)
                  (get-x b0)
                  (get-x a0))
                (/ s-num st-den))
            t (if (== st-den 0)
                (if (== (get-x bn) 0)
                  (get-y a0)
                  (get-y b0))
                (/ t-num st-den))
            hit (+ a0 (* s an))
            hit-dist (structs/euclidean-dist a0 hit)]
        (if (and (>= s 0) (<= 0 t) (<= t bl) (>= hit-dist 0.1))
          {:intersect HIT
            :at hit
            :with mirror
            :distance hit-dist}
          {:intersect MISS}))
      ;; overlap test
      (let [a0-b0-dist (structs/euclidean-dist a0 b0)
            a0-b1-dist (structs/euclidean-dist a0 b1)
            closer (if (< a0-b0-dist a0-b1-dist) b0 b1)
            closer-dist (structs/euclidean-dist a0 closer)
            a0-closer-dir (structs/make-normal-direction a0 closer)]
        (if (== a0-closer-dir an)
          {:intersect OVERLAP
            :at closer
            :with mirror
            :distance closer-dist}
          {:intersect MISS})))))