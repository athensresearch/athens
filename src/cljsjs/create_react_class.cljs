(ns cljsjs.create-react-class
  (:require
    ["create-react-class" :as create-react-class]
    ["react" :as react]))


(js/goog.object.set react "createClass" create-react-class)
(js/goog.exportSymbol "createReactClass" create-react-class)
