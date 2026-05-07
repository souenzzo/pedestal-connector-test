(ns pedestal-connector-test.request.server-name-test
  (:require [clojure.test :refer [deftest is]]
            [pedestal-connector-test.api :as api]))

(deftest server-name
  (is (= true
        (-> {}
          api/capture-request
          :server-name
          string?))))
