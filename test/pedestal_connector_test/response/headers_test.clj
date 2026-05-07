(ns pedestal-connector-test.response.headers-test
  (:require [clojure.test :refer [deftest is]]
            [pedestal-connector-test.api :as api]))

(set! *warn-on-reflection* true)

(comment
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.jetty/create-connector"))

(deftest hello-header
  (is (= {"hello" "world"}
        (-> (constantly {:headers {"hello" "world"}
                         :status  204})
          (api/simple-request {})
          :headers
          (select-keys ["hello"])))))
