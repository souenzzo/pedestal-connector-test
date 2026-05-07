(ns pedestal-connector-test.request.ssl-client-cert-test
  (:require [clojure.test :refer [deftest is]]
            [pedestal-connector-test.api :as api]))
(comment
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.jetty/create-connector"))

(deftest ssl-client-cert
  (is (= false
        (-> {}
          api/capture-request
          (contains? :ssl-client-cert)))))
