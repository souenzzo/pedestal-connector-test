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

(deftest multiple-headers
  (is (= {"hello" (case api/connector-ident
                    :jetty "a,b"
                    :http-kit "a\nb")}
        (-> {:headers {"hello" ["a" "b"]}}
          api/capture-request
          :headers
          (select-keys ["hello"]))))
  (is (= {"hello" (case api/connector-ident
                    :jetty ",,,"
                    :http-kit ",\n,")}
        (-> {:headers {"hello" ["," ","]}}
          api/capture-request
          :headers
          (select-keys ["hello"]))))
  (is (= {"hello" (case api/connector-ident
                    :jetty "a,c\\nd"
                    :http-kit "a\nc\\nd")}
        (-> {:headers {"hello" ["a\n" "c\\nd"]}}
          api/capture-request
          :headers
          (select-keys ["hello"])
          (doto clojure.pprint/pprint)))))
