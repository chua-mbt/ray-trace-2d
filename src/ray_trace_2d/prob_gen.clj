(ns ray-trace-2d.prob-gen
  (:require [ray-trace-2d.structs :as structs])
  (:use [clojure.core.matrix]
        [clojure.core.matrix.operators]))

(def ^:const max-n 500)

(defn rand-n []
  (+ (rand-int max-n) 1))

(defn fold-peri-pt [pt-1d width height]
  (let [perimeter (+ (* 2.0 width) (* 2.0 height))]
    (cond
      (< pt-1d 0.0) (throw (Exception. "pt-1d is less than 0!"))
      (< pt-1d width) (structs/make-point pt-1d 0.0) ;; top
      (< pt-1d (* 2.0 width)) (structs/make-point (- pt-1d width) height) ;; bottom
      (< pt-1d (+ (* 2.0 width) height)) (structs/make-point 0.0 (- pt-1d (* 2.0 width))) ;; left
      (< pt-1d perimeter) (structs/make-point width (- pt-1d (+ (* 2.0 width) height))) ;; right
      :else (throw (Exception. "pt-1d is greater than perimeter!")))))

(defn random-light-dir [origin room]
  (let [width (room :width)
        height (room :height)
        center (structs/make-point (/ width 2.0) (/ height 2.0))
        random-angle (- (rand structs/max-rand-angle) (/ structs/max-rand-angle 2.0))
        random-target (structs/rotate-2d center origin random-angle)
        light-dir (structs/make-normal-direction origin random-target)]
    light-dir))

(defn place-light [room]
  (let [width (room :width)
        height (room :height)
        perimeter (+ (* 2.0 width) (* 2.0 height))
        random-1d (rand perimeter)
        origin (fold-peri-pt random-1d width height)
        direction (random-light-dir origin room)]
    (structs/make-light origin direction)))

(defn get-compartments [room]
  (let [width (room :width)
        height (room :height)
        comp-size (* 2 structs/mirror-len)
        hori-comp (Math/floor (/ width comp-size))
        vert-comp (Math/floor (/ height comp-size))
        total (* hori-comp vert-comp)]
        (map
          (fn [id]
            { :id id
              :x (+ (* (mod id hori-comp) comp-size) (/ comp-size 2))
              :y (+ (* (Math/floor (/ id hori-comp)) comp-size) (/ comp-size 2)) })
          (range total))))

(defn mirror-in-compartment [compartment]
  (let [center (structs/make-point (compartment :x) (compartment :y))
        p1 (+ center (structs/make-point (/ structs/mirror-len 2.0) 0.0))
        p2 (- center (structs/make-point (/ structs/mirror-len 2.0) 0.0))
        angle (rand structs/max-angle)
        rotated-p1 (structs/rotate-2d p1 center angle)
        rotated-p2 (structs/rotate-2d p2 center angle)]
    (structs/make-mirror rotated-p1 rotated-p2)))

(defn place-mirror [compartments mirrors]
  (cond
    (empty? compartments) mirrors
    :else
      (let [open (first compartments)
            remaining (rest compartments)]
            (place-mirror
              remaining
              (conj mirrors (mirror-in-compartment open))))))

(defn place-mirrors [compartments]
  (place-mirror (shuffle compartments) []))

(defn place-walls []
  (let [width structs/room-width
        height structs/room-height
        top-left (structs/make-point 0.0 0.0)
        top-right (structs/make-point width 0.0)
        bot-left (structs/make-point 0.0 height)
        bot-right (structs/make-point width height)]
    [(structs/make-mirror top-left top-right) ;; top
     (structs/make-mirror bot-left bot-right) ;; bottom
     (structs/make-mirror top-left bot-left) ;; left
     (structs/make-mirror top-right bot-right)])) ;; right

(defn generate-problem []
  "Generates a random problem input."
  (let [room (structs/make-room)
        light (place-light room)
        mirrors (vec
                  (concat
                    (place-walls)
                    (place-mirrors (get-compartments room))))]
    (structs/make-problem (rand-n) room mirrors light)))