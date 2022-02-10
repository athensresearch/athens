(ns athens.macros
  "Macro helper fns."
  (:require
    [clojure.core.specs.alpha :as specs]
    [clojure.spec.alpha :as s]))


;; Fix the defn spec so that s/unform creates valid arg lists.
;; https://blog.klipse.tech/clojure/2019/03/08/spec-custom-defn.html#args-of-defn-macro

(defn arg-list-unformer
  [a]
  (vec
    (if (and (coll? (last a)) (= '& (first (last a))))
      (concat (drop-last a) (last a))
      a)))


(s/def ::specs/arg-list
  (s/and
    vector?
    (s/conformer identity arg-list-unformer)
    (s/cat :args (s/* ::specs/binding-form)
           :varargs (s/? (s/cat :amp #{'&} :form ::specs/binding-form)))))


(defn defn-args-xform
  "Transform defn args using xform.
  Args will be conformed using spec prior to xform, and then
  unformed back to be used in macro code.
  You can use a spy function, like athens.common.utils/spy, to pretty print
  the conformed args for inspection."
  [xform args]
  (let [conf  (s/conform ::specs/defn-args args)
        name  (:name conf)
        conf' (xform conf name)]
    (s/unform ::specs/defn-args conf')))


(defn update-bodies
  "Updates the body of conformed defn args. Supports multiple arities."
  [{[arity] :bs :as conf} body-update-fn]
  (case arity
    :arity-1 (update-in conf [:bs 1 :body] body-update-fn)
    :arity-n (update-in conf [:bs 1 :bodies]
                        (fn [bodies]
                          (map (fn [body] (update body :body body-update-fn)) bodies)))))


(defn add-prepost
  "Add a prepost form to a conformed defn body."
  [form [k v :as body]]
  (case k
    :body [:prepost+body {:prepost form
                          :body v}]
    :prepost+body (throw (ex-info "add-prepost does not yet support composing prepost" body))))


(defn update-body-body
  "Updates the body form inside a conformed defn body."
  [xform [k _ :as body]]
  (case k
    :body (update body 1 xform)
    :prepost+body (update-in body [1 :body] xform)))
