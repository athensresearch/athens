(ns athens.self-hosted.save-load
  (:gen-class)
  (:require
    [athens.common.logging :as log]
    [athens.self-hosted.components.fluree :as fluree-comp]
    [athens.self-hosted.event-log :as event-log]
    [clojure.edn :as edn]
    [clojure.string :as string]
    [clojure.tools.cli :refer [parse-opts]]
    [fluree.db.api :as fdb]))


(defn save-log
  [args]
  (let [{:keys [fluree-address
                filename]} args
        comp               (fluree-comp/create-fluree-comp fluree-address)
        events             (event-log/events comp)]
    ;; Save the ledger on file
    ;; TODO : Who should discover the name for file to save?
    (spit filename
          (pr-str (doall events)))
    (-> comp :conn-atom deref fdb/close)))


(defn recover-log
  [args]
  (let [{:keys [fluree-address
                filename]} args
        comp               (fluree-comp/create-fluree-comp fluree-address)
        events             (event-log/recovered-events comp)]
    (spit filename
          (pr-str (doall events)))
    (-> comp :conn-atom deref fdb/close)))


(defn- load-events
  [comp previous-events progress total]
  (event-log/init! comp [])
  (doseq [[id data] previous-events]
    (swap! progress inc)
    (log/info "Processing" id (str "#" @progress "/" total))
    (event-log/add-event! comp id data)))


(defn load-log
  [args]
  (let [{:keys [fluree-address
                filename
                resume]}       args
        comp                   (fluree-comp/create-fluree-comp fluree-address)
        conn                    (-> comp
                                    :conn-atom
                                    deref)
        previous-events        (edn/read-string (slurp filename))
        total                  (count previous-events)
        ledger-exists?         (seq  @(fdb/ledger-info conn event-log/default-ledger))
        progress               (atom 0)
        last-added-event-id    (when resume
                                 (event-log/last-event-id comp))
        previous-events-since-last-added-event
        (when last-added-event-id
          (drop-while (fn [[id]] (not= id last-added-event-id))
                      previous-events))]

    (cond
      (and ledger-exists? (not resume))
      (do
        (log/info "Deleting the current ledger before loading data....")
        @(fdb/delete-ledger conn
                            event-log/default-ledger)
        (log/warn "Please restart the fluree docker."))

      (and (not ledger-exists?) resume)
      (log/warn "Cannot resume because there's no ledger")

      (and ledger-exists? resume (nil? last-added-event-id))
      (log/warn "Cannot resume because there are no events in the ledger to resume from")

      (and ledger-exists? resume last-added-event-id (empty? previous-events-since-last-added-event))
      (log/warn "Cannot resume because the last ledger event (" last-added-event-id ") is not in the backup")

      (and ledger-exists? resume last-added-event-id previous-events-since-last-added-event)
      (do
        (log/info "Resuming load from event" last-added-event-id)
        ;; The first item is the event we already added, drop it.
        (let [previous-events-after-last-added-event (rest previous-events-since-last-added-event)]
          (reset! progress (- total (count previous-events-after-last-added-event)))
          (load-events comp previous-events-after-last-added-event progress total)))

      :else
      (do
        (log/info "Recreating ledger...")
        (event-log/init! comp [])
        (log/info "Loading all events...")
        (load-events comp previous-events progress total)))))


(def cli-options
  ;; An option with a required argument
  [["-a" "--fluree-address ADDRESS" "Fluree address"
    :default "http://localhost:8090"]
   ["-f" "--filename FILENAME" "Name of the file to be saved or loaded"
    :default []]
   ["-r" "--resume" "Attempt to resume a load attempt from the last saved event."
    :default false]
   ["-h" "--help"]])


(defn usage
  [options-summary]
  (->> ["Save or load a ledger"
        ""
        "Usage: program-name [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  save     Save the current ledger"
        "  load     Load the passed ledger"
        "  recover  Recover failed transactions from the current ledger"
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))


(defn error-msg
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))


(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with an error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      ;; help => exit OK with usage summary
      (:help options)           {:exit-message (usage summary) :ok? true}
      ;; errors => exit with description of errors
      errors                    {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (= 1 (count arguments))
           (#{"save" "load" "recover"}
            (first arguments))) {:action (first arguments) :options options}
      ;; failed custom validation => exit with usage summary
      :else                     {:exit-message (usage summary)})))


(defn exit
  [status msg]
  (println msg)
  (System/exit status))


(defn -main
  [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (do
        (case action
          "save"    (save-log options)
          "load"    (load-log options)
          "recover" (recover-log options))
        (System/exit 0)))))
