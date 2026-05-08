(ns pedestal-connector-test.request.headers-test
  (:require [clojure.test :refer [deftest is]]
            [pedestal-connector-test.api :as api]))

(comment
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.jetty/create-connector"))

(deftest headers
  (is (= {"hello" "world"}
        (-> {:headers {"hello" ["world"]}}
          api/capture-request
          :headers
          (select-keys ["hello"])))))
