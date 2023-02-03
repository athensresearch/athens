(ns athens.types.user.db
  (:require
   [athens.common-db                     :as common-db]
   [athens.common-events                 :as common-events]
   [athens.common-events.bfs             :as bfs]
   [athens.common-events.graph.composite :as composite]))


(def USER-ENTITY-TYPE "[[athens/user]]")


(defn internal-representation-new-user-page
  [username]
  [{:page/title       (str "@" username)
    :block/properties {":entity/type" {:block/string USER-ENTITY-TYPE}}}])


(defn create-user-events
  "Generates events to creates new user entity type.

  This implies creating page `@username` page.

  If the `@username` page already exists,
  checks for value of property `:entity/type`.
  Errors when value already exists and is not equal to `USER-ENTITY-TYPE`.
  If exists and is equal to `USER-ENTITY-TYPE` does nothing.
  If doesn't exists creates it with `USER-ENTITY-TYPE` value.

  If `@username` doesn't exists creates it
  and adds `:entity/type` property with `USER-ENTITY-TYPE` value."
  [db username]
  (->> (bfs/internal-representation->atomic-ops db (internal-representation-new-user-page username) :first)
       (composite/make-consequence-op {:op/type :new-user})
       common-events/build-atomic-event))
