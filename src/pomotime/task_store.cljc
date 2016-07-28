(ns pomotime.task-store
  (:require [clojure.spec :as s]
            [clojure.spec :as s]
            [clojure.spec :as s]))

(defprotocol TaskStore
  (-all [this owner])
  (-get [this id])
  (-put! [this task]))

(defn all-tasks [store owner]
  (-all store owner))
(s/fdef all-tasks
  :args (s/cat :store (partial satisfies? TaskStore) :owner :pomotime.task/owner)
  :ret (s/coll-of :pomotime/task)
  :fn (fn [{ret :ret {owner :owner} :args}]
        (every? #(= owner (:pomotime.task/owner %)) ret)))

(defn get-task [store id]
  (-get store id))
(s/fdef get-task
  :args (s/cat :store (partial satisfies? TaskStore) :id :pomotime.task/id)
  :ret (s/nilable :pomotime/task)
  :fn #(or (nil? (:ret %))
           (= (-> % :args :id)
              (-> % :ret :pomotime.task/id))))

(defn put-task! [store task]
  (-put! store task)
  store)
(s/fdef put-task!
  :args (s/cat :store (partial satisfies? TaskStore) :task :pomotime/task)
  :ret (partial satisfies? TaskStore)
  :fn #(identical? (:ret %) (-> % :args :store)))
