(ns oljacl.model.migration
  (:require [clojure.java.jdbc.deprecated :as sql]))

(defn migrated? []
  (not (zero?
        (sql/with-connection (System/getenv "DATABASE_URL")
          (sql/with-query-results results
            ["select count(*) from information_schema.tables where table_name='tests'"]
            (:count (first results)))))))

(defn migrate []
  (when (not (migrated?))
    (print "Pravljenje baze...") (flush)
    (sql/with-connection (System/getenv "DATABASE_URL")
      (sql/create-table :tests
                        [:id :serial "PRIMARY KEY"]
                        [:body :varchar "NOT NULL"]
                        [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]))
    (println " done")))