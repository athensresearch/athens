(ns athens.self-hosted.save-load
  (:require
    [clojure.tools.cli :refer [parse-opts]]
    [fluree.db.api :as fdb]
    [athens.self-hosted.event-log :as event-log]
    [clojure.string :as string])
  (:gen-class))


(defn save-log
  [args]
  (let [{:keys [fluree-address
                filename]} args
        comp               (event-log/create-fluree-comp fluree-address)
        events             (event-log/events comp)]
    ;; Save the ledger on file
    ;; TODO : Who should discover the name for file to save?
    (spit filename
          (pr-str (doall events)))
    (-> comp :conn-atom deref fdb/close)
    (System/exit 0)))


(defn load-log
  [args]
  (let [{:keys [fluree-address
                filename]}     args
        comp                   (event-log/create-fluree-comp fluree-address)
        previous-events        (clojure.edn/read-string (slurp filename))]

    ;; Delete the current ledger
    @(fdb/delete-ledger (-> comp
                            :conn-atom
                            deref)
                        event-log/ledger)

    ;; Create the ledger again
    (event-log/ensure-ledger! comp previous-events)
    (System/exit 0)))



(def cli-options
  ;; An option with a required argument
  [["-a" "--fluree-address ADDRESS" "Fluree address"
    :default "http://localhost:8090"]
   ["-f" "--filename FILENAME" "Name of the file to be saved or loaded"
    :default []]
   ["-h" "--help"]])

(defn usage [options-summary]
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
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))


(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with an error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]}  (parse-opts args cli-options)]
    (cond
      ; help => exit OK with usage summary
      (:help options)                               {:exit-message (usage summary) :ok? true}
      ; errors => exit with description of errors
      errors                                        {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (= 1 (count arguments))
           (#{"save" "load" } (first arguments)))   {:action (first arguments) :options options}
      ; failed custom validation => exit with usage summary
      :else                                         {:exit-message (usage summary)})))


(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (case action
        "save"   (save-log options)
        "load"   (load-log options)))))
