(ns pedestal-connector-test.request.query-string-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [pedestal-connector-test.api :as api]))

(comment
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.jetty/create-connector"))

(deftest query-string
  (is (= "hello"
        (-> {:query-string "hello"}
          api/capture-request
          :query-string))))


(deftest edge-values
  (is (= "%20"
        (-> {:query-string " "}
          api/capture-request
          :query-string)))
  (is (= "%20%20"
        (-> {:query-string "  "}
          api/capture-request
          :query-string)))
  (is (= "/!$&'()*+,-.?~"
        (-> {:query-string "/!$&'()*+,-.?~"}
          api/capture-request
          :query-string)))
  (is (= "%25"
        (-> {:query-string "%"}
          api/capture-request
          :query-string)))
  (is (= "%2520"
        (-> {:query-string "%20"}
          api/capture-request
          :query-string)))
  (is (= "%08%09%0A%0C%0D"
        (-> {:query-string "\b\t\n\f\r"}
          api/capture-request
          :query-string))))

(defspec alphanumeric-query 100
  (prop/for-all [query-string (gen/such-that seq
                                gen/string-alphanumeric)]
    (= query-string
      (-> {:query-string query-string}
        api/capture-request
        :query-string))))
