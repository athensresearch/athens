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
   :shortcut/move])


;; Block

(def child-position
  [:or
   [:map
    [:ref-name string?]
    [:relation [:enum
                :first
                :last]]]
   [:map
    [:ref-uid string?]
    [:relation [:enum
                :first
                :last]]]])


(def sibling-position
  [:map
   [:ref-uid string?]
   [:relation [:enum
               :before
               :after]]])


(def position
  [:or
   child-position
   sibling-position])


(def op-block-new
  [:map
   [:op/args
    [:map
     [:block-uid string?]
     [:position position]]]])


(def op-block-save
  [:map
   [:op/args
    [:map
     [:block-uid string?]
     [:string string?]]]])


(def op-block-open
  [:map
   [:op/args
    [:map
     [:block-uid string?]
     [:open? boolean?]]]])


(def op-block-remove
  [:map
   [:op/args
    [:map
     [:block-uid string?]]]])


(def op-block-move
  [:map
   [:op/args
    [:map
     [:block-uid string?]
     [:position position]]]])


;; Page

(def op-page-new
  [:map
   [:op/args
    [:map
     [:name string?]]]])


(def op-page-rename
  [:map
   [:op/args
    [:map
     [:old-name string?]
     [:new-name string?]]]])


(def op-page-merge
  [:map
   [:op/args
    [:map
     [:from-name string?]
     [:to-name string?]]]])


(def op-page-remove
  [:map
   [:op/args
    [:map
     [:name string?]]]])


;; Shortcut

(def shortcut-position
  [:map
   [:ref-name string?]
   [:relation [:enum
               :before
               :after]]])


(def op-shortcut-new
  [:map
   [:op/args
    [:map
     [:name string?]]]])


(def op-shortcut-remove
  [:map
   [:op/args
    [:map
     [:name string?]]]])


(def op-shortcut-move
  [:map
   [:op/args
    [:map
     [:name string?]
     [:shortcut-position shortcut-position]]]])


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
