(ns athens.views.blocks.types.dispatcher)


(defmulti block-type->protocol
  "Takes `:block/type` value and converts it to `BlockTypeProtocol` to be used for rendering"
  (fn [block-type _args-map]
    ;; TODO: remove debug code
    (println "block-type->protocol:" (pr-str block-type))
    block-type))

