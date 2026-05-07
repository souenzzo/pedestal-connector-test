(ns pedestal-connector-test.request.uri-test
  (:require [clojure.test :refer [deftest is]]
            [pedestal-connector-test.api :as api]))

(comment
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.jetty/create-connector")
  (System/setProperty "pedestal-connector-test.api/port" "8080"))

(deftest hello-uri
  (is (= "/hello"
        (-> {:uri "/hello"}
          api/capture-request
          :uri))))
