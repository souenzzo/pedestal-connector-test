(ns pedestal-connector-test.request.server-port-test
  (:require [clojure.test :refer [deftest is]]
            [pedestal-connector-test.api :as api]))

(comment
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.jetty/create-connector"))

(deftest server-port
  (is (= true
        (-> {}
          api/capture-request
          :server-port
          number?))))
