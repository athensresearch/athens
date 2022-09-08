(ns athens.types.dispatcher)


(defn if-not-disabled
  [block-type feature-flags]
  (let [type->ff {"[[athens/task]]" :tasks
                  "[[athens/query]]" :queries}]
    (if-let [ff (type->ff block-type)]
      (and (feature-flags ff) block-type)
      block-type)))


(defn block-type->protocol-k
  [block-type ff]
  (if-not-disabled block-type ff))


(defmulti block-type->protocol
  "Returns `BlockTypeProtocol` to be used for rendering based on k.
  Use block-type->protocol-k to compute k, and pass it as metadata to reagent components that
  use the renderer as an argument (e.g. `^{:key renderer-k} [some-comp renderer ...]`) to make them reactive.
  Clojure multimethods are always the same fn (this one) independently of what method will be dispatched.
  Thus Reagent/React components will not re-render because the comp arguments did not change.
  Setting the key metadata is a workaround for this problem, see https://stackoverflow.com/a/33461346/2116927"
  (fn [k _args-map]
    #_(println "block-type->protocol:" (pr-str k))
    k))
