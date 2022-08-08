(ns athens.types.dispatcher)


(defmulti block-type->protocol
  "Takes `:block/type` value and converts it to `BlockTypeProtocol` to be used for rendering"
  (fn [block-type _args-map]
    #_(println "block-type->protocol:" (pr-str block-type))
    block-type))

