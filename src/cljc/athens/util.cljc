(ns athens.util)


(defn gen-block-uid
  []
  (subs (str (random-uuid)) 27))


(defn now-ts
  []
  (-> (js/Date.) .getTime))


(defn scrollIfNeeded
  [element container]
  (if (< (.-offsetTop element) (.-scrollTop container))
    (set! (.-scrollTop container) (.-offsetTop element))
    (let [offsetBottom (+ (.-offsetTop element) (.-offsetHeight element))
          scrollBottom (+ (.-scrollTop container) (.-offsetHeight container))]
      (when (> offsetBottom scrollBottom)
        (set!
         (.-scrollTop container)
         (- offsetBottom (.-offsetHeight container)))))))