(ns athens.views.notifications
  (:require
   ["/components/inbox/Inbox" :refer [Inbox]]
   ["@chakra-ui/react" :refer [Text HStack VStack Heading Box Modal ModalOverlay ModalContent ModalHeader ModalBody ModalCloseButton]]
   [athens.util :as util]
   [clojure.string :as str]
   [re-frame.core :refer [dispatch subscribe]]
   [reagent.core :as r]))


(defn inbox
  []
  [:> Inbox])