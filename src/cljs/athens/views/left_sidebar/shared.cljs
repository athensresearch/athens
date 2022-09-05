(ns athens.views.left-sidebar.shared)


(def NS "athens/left-sidebar")


(defn ns-str
  ([]
   (ns-str ""))
  ([s]
   (str NS s))
  ([s & ss]
   (apply str NS s ss)))


