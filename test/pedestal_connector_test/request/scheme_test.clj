(ns pedestal-connector-test.request.scheme-test
  (:require [clojure.test :refer [deftest is]]
            [pedestal-connector-test.api :as api]))

(deftest scheme
  (is (= true
        (-> {}
          api/capture-request
          :scheme
          keyword?))))
