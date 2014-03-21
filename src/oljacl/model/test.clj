(ns oljacl.model.test
    (:require [clojure.java.jdbc.deprecated :as sql]))

(defn all []
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/with-query-results results
      ["select * from tests order by id desc"]
      (into [] results))))

(defn create [test]
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/insert-values :tests [:body] [test])))