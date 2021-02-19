(ns athens.dbrx
  (:require
    [athens.db :as db]))


(def pull-many
  (fn [& args]
    (try (apply db/pull*-rx args)
         (catch :default e
           (throw (js/Error. (str "pull-many problem "
                                  args)))))))


(def pull
  (fn [& args]
    (try
      (apply db/pull-rx args)
      (catch :default e
        (throw (js/Error. (str "pull problem "
                               args)))))))


(def q
  (fn [& args]
    (try
      (apply db/q-rx args)
      (catch :default e
        (str "q problem: "
             args)))))


(def transact!
  (fn [& args]
    (try
      (apply db/transact! args)
      (catch :default e
        (str "transact! problem: "
             args)))))

