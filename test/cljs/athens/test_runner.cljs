(ns athens.test-runner
  (:require  [doo.runner :refer-macros [doo-tests]]
             [athens.db-test]
             [athens.block-test]))

(enable-console-print!)

(doo-tests 'athens.db-test
           'athens.block-test)

