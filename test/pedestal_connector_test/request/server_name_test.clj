(ns pedestal-connector-test.request.server-name-test
  (:require [clojure.string :as string]
            [clojure.test :refer [deftest is]]
            [pedestal-connector-test.api :as api]))

(comment
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.jetty/create-connector")

  (deftest server-name
    (is (= true
          (-> {}
            api/capture-request
            :server-name
            string?)))))

(deftest overwrite-server-name
  (when (some-> "jdk.httpclient.allowRestrictedHeaders"
          System/getProperty
          (string/includes? "host"))
    (is (= "pedestal.io"
          (-> {:headers {"Host" ["pedestal.io"]}}
            api/capture-request
            :server-name)))
    (is (= "_-;*$!"
          (-> {:headers {"Host" ["_-;*$!"]}}
            api/capture-request
            :server-name)))))
