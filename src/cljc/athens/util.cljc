(ns athens.util)


(defn gen-block-uid
  []
  (subs (str (random-uuid)) 27))


(defn now-ts
  []
  (-> (js/Date.) .getTime))


(defn scroll-if-needed
  ;; https://stackoverflow.com/a/45851497
  [element container]
  (if (< (.. element -offsetTop) (.. container -scrollTop))
    ;; If the element is higher than its container's top...
    (set! (.. container -scrollTop) (.. element -offsetTop))
    ;; Otherwise, find the bottom of the element and the container...
    (let [offsetBottom (+ (.. element -offsetTop) (.. element -offsetHeight))
          scrollBottom (+ (.. container -scrollTop) (.. container -offsetHeight))]
      ;; ..and if it's lower than the container's bottom
      (when (< scrollBottom offsetBottom)
        ;; Scroll the container so the element is in view
        (set!
          (.. container -scrollTop)
          (- offsetBottom (.. container -offsetHeight)))))))
