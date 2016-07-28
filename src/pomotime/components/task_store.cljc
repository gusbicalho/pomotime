(ns pomotime.components.task-store
  (:require [pomotime.task-store :as ts]
            [com.stuartsierra.component :as component]
            [clojure.spec :as s]))

(s/def ::tasks
  (s/every (s/and (s/cat :id :pomotime.task/id :task :pomotime/task)
                  #(= (:id %) (-> % :task :pomotime.task/id)))
           :kind map?))

(s/def ::owner->tasks
  (s/every (s/cat :owner :pomotime.task/owner
                  :tasks (s/coll-of :pomotime.task/id))
           :kind map?))

(s/def ::store-value (s/keys :req [::tasks ::owner->tasks]))

(defn- all-tasks [store-value owner]
  (->> [::owner->tasks owner]
       (get-in store-value)
       (mapv (:tasks store-value))))
(s/fdef all-tasks
  :args (s/cat :store-value ::store-value :owner :pomotime.task/owner)
  :ret (s/coll-of :pomotime/task)
  :fn (fn [{ret :ret {owner :owner} :args}]
        (every? #(= owner (:pomotime.task/owner %)) ret)))

(defn- get-task [store-value id]
  (get-in store-value [::tasks id]))
(s/fdef get-task
  :args (s/cat :store-value ::store-value :id :pomotime.task/id)
  :ret (s/nilable :pomotime/task)
  :fn (s/or :none (comp nil? :ret)
            :some #(= (-> % :args :id)
                      (-> % :ret :pomotime.task/id))))

(defn- ownership-mismatch [store-value {:keys [pomotime.task/id pomotime.task/owner]}]
  (when-let [task (get-task store-value id)]
    (when-not (= owner (:pomotime.task/owner task))
      task)))

(defn- put-task [store-value {:keys [pomotime.task/id pomotime.task/owner] :as task}]
  (if-let [{existing-owner :pomotime.task/owner} (get-task store-value id)]
    (if (= owner existing-owner)
      (update store-value ::tasks assoc id task)
      store-value)
    (-> store-value
        (update ::tasks assoc id task)
        (update-in [::owner->tasks owner] conj id))))
(s/fdef put-task
  :args (s/cat :store-value ::store-value :task :pomotime/task)
  :ret ::store-value
  :fn (s/or :unchanged (s/and #(ownership-mismatch (:ret %) (-> % :args :task))
                              #(= (:ret %) (-> % :args :store-value)))
            :inserted (s/and #(= (-> % :args :task)
                                 (->> % :args :task :pomotime.task/id (get-task (:ret %))))
                             #(some (-> % :args :task :pomotime.task/id hash-set)
                                    (->> % :args :task :pomotime.task/owner (all-tasks (:ret %)))))))

(defn- check-ownership-matches! [store-value {:keys [pomotime.task/owner] :as new-task}]
  (when-let [task (ownership-mismatch store-value new-task)]
    (throw (ex-info "Forbidden" {:type    :forbidden
                                 :details {:attempted-owner owner
                                           :existing-task   task}}))))

(defrecord AtomTaskStore []
  component/Lifecycle
  (start [this]
    (assoc this :store (atom {::tasks        {}
                              ::owner->tasks {}})))
  (stop [this]
    (reset! (:store this) nil)
    (dissoc this :store))
  ts/TaskStore
  (-all [{:keys [store]} owner]
    (all-tasks @store owner))
  (-get [{:keys [store]} id]
    (get-task @store id))
  (-put! [{:keys [store]} task]
    (swap! store put-task task)
    (check-ownership-matches! @store task)))
