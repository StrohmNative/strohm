(ns app.main)

(defn main! []
  (println "[main] started"))

(defn ^:dev/after-load reload! []
  (println "[main] reloaded"))
