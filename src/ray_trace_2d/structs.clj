(ns ray-trace-2d.structs
  (:use [clojure.core.matrix]
        [clojure.core.matrix.operators]))

(def ^:const mirror-len 30.0)
(def ^:const room-width 600.0)
(def ^:const room-height 600.0)
(def ^:const max-angle 360.0)
(def ^:const max-rand-angle 80.0)

(defn get-x [vec-2d]
  (first vec-2d))

(defn get-y [vec-2d]
  (second vec-2d))

(defn make-point [x y]
  (matrix [x y]))

(defn make-raw-direction [x y]
  (matrix [x y]))

(defn make-direction [src des]
  (- des src))

(defn make-normal-direction [src des]
  (normalise (make-direction src des)))

(defn make-room []
  {:width room-width, :height room-height})

(defn make-mirror [p1 p2]
  {:p1 p1, :p2 p2})

(defn make-light [origin direction]
  {:origin origin, :direction direction})

(defn make-problem [n room mirrors light]
  {:n n :room room, :mirrors mirrors, :light light})

(defn make-solution [problem]
  {:reflections [((problem :light) :origin)]
    :last-dir ((problem :light) :direction)})

(defn extend-solution [solution reflect-pt reflect-dir]
  {:reflections (conj (solution :reflections) reflect-pt)
    :last-dir reflect-dir})

(defn rotate-2d [pt pivot angle]
  (let [theta (Math/toRadians angle)
        shifted (- pt pivot)
        cs (Math/cos theta)
        sn (Math/sin theta)
        rotated
          (make-point
            (- (* (get-x shifted) cs) (* (get-y shifted) sn))
            (+ (* (get-x shifted) sn) (* (get-y shifted) cs)))
        returned (+ rotated pivot)]
    returned))

(defn euclidean-dist [p1 p2]
  (length (- p2 p1)))

(defn normal-rot [dir]
  (rotate-2d dir (make-point 0.0 0.0) 90.0))
