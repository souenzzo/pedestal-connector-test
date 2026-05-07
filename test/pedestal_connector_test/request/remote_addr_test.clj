(ns pedestal-connector-test.request.remote-addr-test
  (:require [clojure.test :refer [deftest is]]
            [pedestal-connector-test.api :as api]))

(deftest remote-addr
  (is (= true
        (-> {}
          api/capture-request
          :remote-addr
          string?))))
