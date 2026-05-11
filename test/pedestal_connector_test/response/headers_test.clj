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

(deftest multiple-headers
  (is (= {"hello" ["a" "b"]}
        (-> (constantly {:headers {"hello" ["a" "b"]}
                         :status  204})
          (api/simple-request {})
          :headers
          (select-keys ["hello"])
          #_(doto clojure.pprint/pprint))))
  (is (= {"hello" ["a" "b" "c" "d"]}
        (-> (constantly {:headers {"hello" ["a" "b"]
                                   "Hello" ["c" "d"]}
                         :status  204})
          (api/simple-request {})
          :headers
          (select-keys ["hello"])
          #_(doto clojure.pprint/pprint))))
  (is (= {"hello" ["a" "b" "a" "b"]}
        (-> (constantly {:headers {"hello" ["a" "b"]
                                   "hellO" ["a" "b"]}
                         :status  204})
          (api/simple-request {})
          :headers
          (select-keys ["hello"])
          #_(doto clojure.pprint/pprint)))))
