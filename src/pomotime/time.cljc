(ns pomotime.time
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.spec.gen :as gen])
  (:import [java.time LocalDateTime ZonedDateTime ZoneOffset Instant]
           [java.time.format DateTimeFormatter]
           ))

(defn- iso-string->zoned-at-utc [iso-8601-string]
  (-> (ZonedDateTime/parse iso-8601-string DateTimeFormatter/ISO_DATE_TIME)
      (.withZoneSameInstant ZoneOffset/UTC)))

(defn string->datetime [iso-8601-string]
  (-> iso-8601-string
      (ZonedDateTime/parse DateTimeFormatter/ISO_DATE_TIME)
      (.withZoneSameInstant ZoneOffset/UTC)
      .toLocalDateTime))

(defn inst->local-date-time [inst]
  (LocalDateTime/ofInstant (Instant/ofEpochMilli (.getTime inst)) ZoneOffset/UTC))

(defn datetime? [x]
  (instance? LocalDateTime x))

(s/def ::datetime (s/with-gen datetime? #(gen/fmap inst->local-date-time (s/gen inst?))))
