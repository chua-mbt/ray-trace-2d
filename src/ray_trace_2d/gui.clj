(ns ray-trace-2d.gui
  (:require [seesaw.core :as seesaw]
            [seesaw.graphics :as seesaw-gfx]
            [ray-trace-2d.soln-gen :as soln-gen]
            [ray-trace-2d.serialize :as serialize])
  (:use [clojure.core.matrix]
        [clojure.core.matrix.operators])
  (:import [java.awt.event KeyEvent]))

(def ^:const light-prev-len 15.0)
(def not-nil? (complement nil?))

(def file "dump.txt")
(def problem nil)
(def solution nil)

(defn draw-mirror [c g mirrors]
  (cond
    (empty? mirrors) nil
    :else (let [mirror-style (seesaw-gfx/style
                  :foreground "#000000" :stroke 1 :cap :round)
                mirror (first mirrors)
                p1 (mirror :p1)
                p2 (mirror :p2)]
      (seesaw-gfx/draw g
        (seesaw-gfx/polygon
          [(first p1) (second p1)]
          [(first p2) (second p2)])
        mirror-style)
      (draw-mirror c g (rest mirrors)))))

(defn draw-mirrors [c g mirrors]
  (draw-mirror c g mirrors))

(defn draw-light [c g light]
  (let [light-style (seesaw-gfx/style
          :foreground "#FF0000" :stroke 2 :cap :round)
        origin (light :origin)
        light-prev-dir (+ origin (* light-prev-len (light :direction)))]
    (seesaw-gfx/draw g
      (seesaw-gfx/polygon
        [(first origin) (second origin)]
        [(first light-prev-dir) (second light-prev-dir)])
      light-style)))

(defn draw-reflections [c g reflections]
  (let [beam-style (seesaw-gfx/style
          :foreground "#FF0000" :stroke 1 :cap :round)
        last-style (seesaw-gfx/style
          :foreground "#0000FF" :stroke 2 :cap :round)
        last-bounce (last reflections)]
    (reduce
      (fn [origin bounce]
        (seesaw-gfx/draw g
          (seesaw-gfx/polygon
            [(first origin) (second origin)]
            [(first bounce) (second bounce)])
          (if (= last-bounce bounce) last-style beam-style))
        bounce)
      reflections)))

(declare soln-forward soln-dump)

(defn set-title [frame]
  (seesaw/config! frame :title
    (str
      "Ray Tracer GUI"
      (if (not-nil? solution)
        (str
          " : ("
          (serialize/solution-to-string solution)
          ") "
          "n = "
          (- (count (solution :reflections)) 2))))))

(defn make-listeners [soln-frame]
  (fn [e]
    (let [key-code (.getKeyCode e)]
      (cond
        (= key-code KeyEvent/VK_RIGHT)
          (soln-forward soln-frame)
        (= key-code KeyEvent/VK_D)
          (soln-dump soln-frame)))))

(defn make-frame [problem]
  (seesaw/frame
    :resizable? false,
    :content
      (seesaw/canvas
        :id :frame-disp,
        :preferred-size
          [((problem :room) :width) :by ((problem :room) :height)],
        :paint (fn [c g]
          (draw-mirrors c g (problem :mirrors))
          (draw-light c g (problem :light))
          (if (not-nil? solution)
            (draw-reflections c g (solution :reflections))))),
    :on-close :exit))

(defn soln-forward [soln-frame]
  (let [canvas (seesaw/select soln-frame [:#frame-disp])
        extended (soln-gen/solution-step problem solution)]
    (def solution extended)
    (set-title soln-frame)
    (seesaw/repaint! canvas)))

(defn soln-dump [soln-frame]
  (serialize/solution-to-file
    file solution))

(defn start-prob-gui [prob-arg]
  (def problem prob-arg)
  (seesaw/native!)
  (seesaw/invoke-later
    (->
      (make-frame problem)
      seesaw/pack!
      seesaw/show!)))

(defn start-soln-gui [file-arg prob-arg soln-arg]
  (def file file-arg)
  (def problem prob-arg)
  (def solution soln-arg)
  (seesaw/native!)
  (seesaw/invoke-later
    (let [soln-frame (make-frame problem)]
      (-> soln-frame
        seesaw/pack!
        seesaw/show!)
      (set-title soln-frame)
      (seesaw/listen soln-frame
        :key-pressed (make-listeners soln-frame)))))