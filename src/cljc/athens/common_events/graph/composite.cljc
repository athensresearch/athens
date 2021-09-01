(ns athens.common-events.graph.composite
  "⎄ Composite Graph Ops.")


(defn make-consequence-op
  "Creates Consequence Operation.
   - `trigger` - trigger event, either semantic event or another composite operation.
   - `consequences` - seq of consequence operation (atomic or composite operations)"
  [trigger consequences]
  {:op/type         :composite/consequence
   :op/atomic?      false
   :op/trigger      trigger
   :op/consequences consequences})
