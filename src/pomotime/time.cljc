(ns pomotime.time
  (:import [java.time LocalDateTime ZonedDateTime ZoneOffset]
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

(defn datetime? [x]
  (instance? LocalDateTime x))
