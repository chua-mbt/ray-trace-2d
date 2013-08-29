(ns ray-trace-2d.serialize
  (:require [ray-trace-2d.structs :as structs]
            [clojure.string :as cstr]))

(defn vec-to-string [vec-2d]
  (str
    (format "%.2f" (structs/get-x vec-2d))
    " "
    (format "%.2f" (structs/get-y vec-2d))))

(defn string-to-vec-2d [string]
  (let [vec-2d (cstr/split string #" ")
        x (read-string (first vec-2d))
        y (read-string (second vec-2d))]
    (structs/make-point x y)))

(defn string-to-file [file string]
  (spit file string))

(defn light-to-string [light]
  (str
    (vec-to-string (light :origin))
    ","
    (vec-to-string (light :direction))))

(defn string-to-light [string]
  (let [vecs (cstr/split string #",")
        origin (string-to-vec-2d (first vecs))
        direction (string-to-vec-2d (second vecs))]
    (structs/make-light origin direction)))

(defn mirrors-to-string [mirrors]
  (cstr/join
    ";"
    (map
      (fn [mirror]
        (str
          (vec-to-string (mirror :p1))
          ","
          (vec-to-string (mirror :p2))))
      mirrors)))

(defn string-to-mirror [string]
  (let [vecs (cstr/split string #",")
        p1 (string-to-vec-2d (first vecs))
        p2 (string-to-vec-2d (second vecs))]
    (structs/make-mirror p1 p2)))

(defn strings-to-mirrors [strings]
  (map string-to-mirror strings))

(defn problem-to-string [problem]
  (str
    (str (problem :n))
    ";"
    (light-to-string (problem :light))
    ";"
    (mirrors-to-string (problem :mirrors))
    ))

(defn string-to-problem [string]
  (let [input (cstr/split string #";")
        n (read-string (first input))
        light (string-to-light (first (rest input)))
        mirrors (strings-to-mirrors (rest (rest input)))]
    (structs/make-problem n (structs/make-room) mirrors light)))

(defn problem-to-file [file problem]
  (string-to-file file (problem-to-string problem)))

(defn file-to-problem [file]
  (string-to-problem (slurp file)))

(defn solution-to-string [solution]
  ;;(println (solution :reflections))
  ;;(println (solution :last-dir))
  (vec-to-string (last (solution :reflections))))

(defn solution-to-file [file solution]
  (string-to-file file (solution-to-string solution)))

(defn strs-to-file [file strs]
  (string-to-file file (cstr/join \newline strs)))

(defn prob-strs-to-file [file problems]
  (strs-to-file file problems))

(defn soln-strs-to-file [file solutions]
  (strs-to-file file solutions))

(defn file-to-prob-strs [file]
  (cstr/split (slurp file) #"\n"))