(ns ray-trace-2d.core
  (:require [ray-trace-2d.structs :as structs]
            [ray-trace-2d.prob-gen :as prob-gen]
            [ray-trace-2d.soln-gen :as soln-gen]
            [ray-trace-2d.serialize :as serialize]
            [ray-trace-2d.gui :as gui])
  (:use [clojure.core.matrix]
        [clojure.tools.cli :only (cli)])
  (:gen-class))

(def ^:const samples 100)

(defn generate-prob-sample [i]
  (let [problem (prob-gen/generate-problem)
        problem-str (serialize/problem-to-string problem)]
    problem-str))

(defn generate-soln-sample [problem-str]
  (let [reloaded (serialize/string-to-problem problem-str)
        solution (soln-gen/generate-solution reloaded)
        solution-str (serialize/solution-to-string solution)]
    solution-str))

(defn generate-tests [prob-file soln-file]
  (let [problems (serialize/file-to-prob-strs prob-file)
        solutions (map generate-soln-sample problems)]
    (serialize/soln-strs-to-file soln-file solutions)))

(defn generate-samples [prob-file soln-file]
  (let [problems (map generate-prob-sample (range samples))
        solutions (map generate-soln-sample problems)]
    (serialize/prob-strs-to-file prob-file problems)
    (serialize/soln-strs-to-file soln-file solutions)))

(defn -main [& args]
  (set-current-implementation :vectorz)
  (let [[opts args banner]
        (cli args
            ["-h" "--help" "Show help" :flag true :default false]
            ["--mode" "Generate 'prob', 'soln' 'samples', 'test'."]
            ["--in" "Input for 'soln' or 'test'; Problem file for 'samples'."]
            ["--out" "Output for 'prob', 'test' or 'soln'; Solution file for 'samples'."]
            ["-g" "--graphics" "GUI for 'prob' or 'soln'" :flag true :default false])]
    (when (:help opts)
      (println banner)
      (System/exit 0))
    (cond
      (and (= (:mode opts) "test") (:in opts) (:out opts))
        (generate-tests (:in opts) (:out opts))
      (and (= (:mode opts) "samples") (:in opts) (:out opts))
        (generate-samples (:in opts) (:out opts))
      (and (= (:mode opts) "prob") (:out opts))
        (let [problem (prob-gen/generate-problem)]
          (serialize/problem-to-file
            (:out opts)
            problem)
          (if (:graphics opts)
            (gui/start-prob-gui
              problem)))
      (and (= (:mode opts) "soln") (:in opts) (:out opts))
        (let [problem (serialize/file-to-problem (:in opts))
              solution (soln-gen/generate-solution problem)]
          (serialize/solution-to-file
            (:out opts)
            solution)
          (if (:graphics opts)
            (gui/start-soln-gui
              (:out opts) problem solution)))
      :else (println banner))))