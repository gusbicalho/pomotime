(ns pomotime.task
  (:require [clojure.spec :as s]
            [clojure.pprint :refer [pprint]]
            [pomotime.time :as time]))

(s/def ::id uuid?)
(s/def ::owner uuid?)
(s/def ::description string?)
(s/def ::deadline :pomotime.time/datetime)
(s/def ::tags (s/coll-of keyword?))
(s/def :pomotime/task (s/and (s/keys :req [::id ::owner ::description]
                                     :opt [::deadline ::tags])))
