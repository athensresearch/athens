(ns athens.util)


(defn gen-block-uid
  []
  (subs (str (random-uuid)) 27))


(defn now-ts
  []
  (-> (js/Date.) .getTime))


;; TODO: move all these DOM utilities to a .cljs file instead of cljc
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


(defn mouse-offset
  [e]
  (let [rect (.. e -target getBoundingClientRect)
        offset-x (- (.. e -pageX) (.. rect -left))
        offset-y (- (.. e -pageY) (.. rect -top))]
    {:x offset-x :y offset-y}))


(defn vertical-center
  [el]
  (let [rect (.. el getBoundingClientRect)]
    (-> (- (.. rect -bottom)
           (.. rect -top))
        (/ 2))))


(defn is-beyond-rect?
  "Checks if any part of the element is above or below the container's bounding rect"
  [element container]
  (let [el-box (.. element getBoundingClientRect)
        cont-box (.. container getBoundingClientRect)]
    (or
      (> (.. el-box -bottom) (.. cont-box -bottom))
      (< (.. el-box -top) (.. cont-box -top)))))
