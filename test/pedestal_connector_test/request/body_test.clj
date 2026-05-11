(ns pedestal-connector-test.request.body-test
  (:require [clojure.test :refer [deftest is]]
            [pedestal-connector-test.api :as api])
  (:import (java.io InputStream)
           (java.net.http HttpRequest$BodyPublishers)))

(comment
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.jetty/create-connector"))

(deftest body
  (is (= true
        (-> {:request-method :post
             :body           (HttpRequest$BodyPublishers/ofString "ok")}
          api/capture-request
          :body
          (->> (instance? InputStream)))))
  (is (= "ok"
        (-> {:request-method :post
             :body           (HttpRequest$BodyPublishers/ofString "ok")}
          (api/capture-request slurp)
          :body))))
