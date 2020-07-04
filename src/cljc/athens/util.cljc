(ns athens.util)


(defn gen-block-uid
  []
  (subs (str (random-uuid)) 27))


(defn now-ts
  []
  (-> (js/Date.) .getTime))

