(ns pedestal-connector-test.request.protocol-test
  (:require [clojure.test :refer [deftest is]]
            [pedestal-connector-test.api :as api]))

(comment
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.jetty/create-connector"))

(deftest protocol
  (when-not (contains? #{:http-kit} api/connector-ident)
    (is (= "HTTP/1.1"
          (-> {:protocol "HTTP/1.1"}
            api/capture-request
            :protocol)))
    (is (= "HTTP/2.0"
          (-> {:protocol "HTTP/2.0"}
            api/capture-request
            :protocol)))))
