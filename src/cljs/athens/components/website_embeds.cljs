(ns athens.components.website-embeds)


(def component-youtube-embed
  {:match  #"\[\[youtube\]\]\:.*"
   :render (fn [content uid]
             ((constantly nil) uid)
             [:span [:iframe {:width       640
                              :height      360
                              :src         (str "https://www.youtube.com/embed/" (get (re-find #".*v=([a-zA-Z0-9_\-]+)" content) 1))
                              :allow       "accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"}]])})


(defn find-weblink
  [content]
  (re-find #"http.*" content))


(def component-generic-embed
  {:match  #"iframe\:.*"
   :render (fn [content uid]
             ((constantly nil) uid)
             [:span [:iframe {:width       640
                              :height      360
                              :src         (find-weblink content)}]])})


(def components [component-youtube-embed component-generic-embed])

