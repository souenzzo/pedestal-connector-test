(ns pedestal-connector-test.response.headers-test
  (:require [clojure.test :refer [deftest is]]
            [io.pedestal.connector :as conn]
            [pedestal-connector-test.api :as api])
  (:import (java.lang AutoCloseable)
           (java.net URI)
           (java.net.http HttpClient HttpClient$Version HttpRequest HttpResponse$BodyHandlers)))

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

(deftest host-on-multiple-requests
  (let [server-port (Long/getLong "pedestal-connector-test.api/port" 1337)
        http-client  (HttpClient/newHttpClient)
        conn (-> server-port
               conn/default-connector-map
               (conn/with-interceptor {:name  :hello
                                       :enter (fn [{:keys [request]
                                                    :as   ctx}]
                                                (def _request request)
                                                (assoc ctx :response {:body   (str (or (get (:headers request) "host")
                                                                                     (keys (:headers request))))
                                                                      :status 200}))})
               (api/create-connector {})
               conn/start!)]
    (try
      (is (= "localhost:1337"
            (-> (URI. "http" nil "localhost" server-port "/" nil nil)
              HttpRequest/newBuilder
              (.version HttpClient$Version/HTTP_2)
              .build
              (as-> % (.send http-client % (HttpResponse$BodyHandlers/ofString)))
              .body)))
      (is (= "localhost:1337"
            (-> (URI. "http" nil "localhost" server-port "/" nil nil)
              HttpRequest/newBuilder
              (.version HttpClient$Version/HTTP_2)
              .build
              (as-> % (.send http-client % (HttpResponse$BodyHandlers/ofString)))
              .body)))
      (finally
        (conn/stop! conn)))))

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
