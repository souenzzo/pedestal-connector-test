(ns pedestal-connector-test.request.headers-test
  (:require [clojure.test :refer [deftest is]]
            [pedestal-connector-test.api :as api]))

(deftest headers
  (is (= {"hello" "world"}
        (-> {:headers {"hello" ["world"]}}
          api/capture-request
          :headers
          (select-keys ["hello"])))))
