(ns pedestal-connector-test.response.body-test
  (:require [clojure.test :refer [deftest is]]
            [pedestal-connector-test.api :as api])
  (:import (java.net.http HttpResponse$BodyHandlers)))

(deftest ok-body
  (is (= "ok"
        (-> (constantly {:body   "ok"
                         :status 200})
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          :body))))
