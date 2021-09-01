(ns athens.common-events.graph.composite
  "⎄ Composite Graph Ops.")


(defn make-consequence-op
  "Creates Consequence Operation.
   - `trigger` - trigger operation/event
   - `consequences` - seq of consequence operation"
  [trigger consequences]
  {:op/type         :composite/consequence
   :op/trigger      trigger
   :op/consequences consequences})
