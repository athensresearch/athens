(ns cljsjs.create-react-class
  (:require
    ["react" :as react]
    ["create-react-class" :as create-react-class]))

(js/goog.object.set react "createClass" create-react-class)
(js/goog.exportSymbol "createReactClass" create-react-class)