(ns pomotime.task
  (:require [clojure.spec :as s]
            [clojure.pprint :refer [pprint]]
            [pomotime.time :as time]
            [pomotime.misc :as misc]))

(defn children-belong? [{:keys [pomotime.task/id pomotime.task/children]}]
  (every? #(= id (::parent-id %)) children))

(defn belongs-to-parent? [task]
  (or (not (contains? task ::parent))
      (= (::parent-id task) (-> task ::parent ::id))))

(s/def ::id uuid?)
(s/def ::description string?)
(s/def ::deadline time/datetime?)
(s/def ::parent-id uuid?)
(s/def ::children (s/coll-of :pomotime/task))
(s/def ::parent :pomotime/task)
(s/def :pomotime/task (s/and (s/keys :req [::id ::description]
                                     :opt [::deadline ::parent-id ::children ::parent])
                             belongs-to-parent?
                             children-belong?))
#_
(pprint
  (let [parent-id (misc/uuid)]
    (s/conform :pomotime/task
      {::id          parent-id
       ::description "Learn spec"
       ::children    [{::id          (misc/uuid)
                       ::parent-id   parent-id
                       ::description "Make a sample task-list app"
                       ::deadline    (time/string->datetime "2016-07-31T12:00:00Z")}
                      ]})))
#_
(pprint
  (let [parent-id (misc/uuid)]
    (s/explain-data :pomotime/task
      {::id          (misc/uuid)
       ::parent-id   parent-id
       ::parent      {::id          parent-id
                      ::description "Learn spec"}
       ::description "Make a sample task-list app"
       ::deadline    (time/string->datetime "2016-07-31T12:00:00Z")})))
