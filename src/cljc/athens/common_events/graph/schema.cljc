(ns athens.common-events.graph.schema
  (:require
    [malli.core  :as m]
    [malli.error :as me]
    [malli.util  :as mu]))


(def atomic-op-types
  [:enum
   :block/new    ; ✓
   :block/save   ; ✓
   :block/open   ; ✓
   :block/remove ; ✓
   :block/move   ; ✓
   :page/new     ; ✓
   :page/rename  ; ✓
   :page/merge   ; ✓
   :page/remove  ; ✓
   :shortcut/new
   :shortcut/remove
   :shortcut/move
   :comment/add])


;; Identity

(def block-id [:block/uid string?])

(def page-id [:page/title string?])


;; Block

(def child-position
  [:or
   [:map
    page-id
    [:relation [:enum
                :first
                :last]]]
   [:map
    block-id
    [:relation [:enum
                :first
                :last]]]])


(def sibling-position
  [:map
   block-id
   [:relation [:enum
               :before
               :after]]])


(def block-position
  [:or
   child-position
   sibling-position])


(def op-block-new
  [:map
   [:op/args
    [:map
     block-id
     [:block/position block-position]]]])


(def op-block-save
  [:map
   [:op/args
    [:map
     block-id
     [:block/string string?]]]])


(def op-block-open
  [:map
   [:op/args
    [:map
     block-id
     [:block/open? boolean?]]]])


(def op-block-remove
  [:map
   [:op/args
    [:map
     block-id]]])


(def op-block-move
  [:map
   [:op/args
    [:map
     block-id
     [:block/position block-position]]]])


(def op-new-comment
  [:map
   [:op/args
     [:map
       block-id
       [:new-comment [:map
                       block-id
                       [:block/type [:enum :comment]]
                       [:author   string?]
                       [:string   string?]
                       [:time     string?]]]]]])



;; Page

(def op-page-new
  [:map
   [:op/args
    [:map
     page-id]]])


(def op-page-rename
  [:map
   [:op/args
    [:map
     page-id
     [:target
      [:map
       page-id]]]]])


(def op-page-merge
  [:map
   [:op/args
    [:map
     page-id
     [:target
      [:map
       page-id]]]]])


(def op-page-remove
  [:map
   [:op/args
    [:map
     page-id]]])


;; Shortcut

(def shortcut-position
  [:map
   page-id
   [:relation [:enum
               :before
               :after]]])


(def op-shortcut-new
  [:map
   [:op/args
    [:map
     page-id]]])


(def op-shortcut-remove
  [:map
   [:op/args
    [:map
     page-id]]])


(def op-shortcut-move
  [:map
   [:op/args
    [:map
     page-id
     [:shortcut/position shortcut-position]]]])


;; Registry

(def op-type-atomic-common
  [:map
   [:op/type atomic-op-types]
   [:op/atomic? true?]])


(def with-common
  (partial mu/merge op-type-atomic-common))


(def atomic-op
  [:schema
   {:registry
    {::atomic-op    [:multi {:dispatch :op/type}
                     [:block/new       (with-common op-block-new)]
                     [:block/save      (with-common op-block-save)]
                     [:block/open      (with-common op-block-open)]
                     [:block/remove    (with-common op-block-remove)]
                     [:block/move      (with-common op-block-move)]
                     [:page/new        (with-common op-page-new)]
                     [:page/rename     (with-common op-page-rename)]
                     [:page/merge      (with-common op-page-merge)]
                     [:page/remove     (with-common op-page-remove)]
                     [:shortcut/new    (with-common op-shortcut-new)]
                     [:shortcut/remove (with-common op-shortcut-remove)]
                     [:shortcut/move   (with-common op-shortcut-move)]
                     [:comment/add     (with-common op-new-comment)]
                     [:composite/consequence [:ref ::composite-op]]]
     ::composite-op [:map
                     [:op/type [:enum :composite/consequence]]
                     [:op/atomic? false?]
                     [:op/trigger map?]
                     [:op/consequences [:sequential [:ref ::atomic-op]]]]}}
   ::atomic-op])


(def valid-atomic-op?
  (m/validator atomic-op))


(defn explain-atomic-op
  [data]
  (-> atomic-op
      (m/explain data)
      (me/humanize)))
