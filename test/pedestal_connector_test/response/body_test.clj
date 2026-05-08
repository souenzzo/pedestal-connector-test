(ns pedestal-connector-test.response.body-test
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [io.pedestal.http.jetty]
            [io.pedestal.interceptor :as interceptor]
            [pedestal-connector-test.api :as api])
  (:import (java.io ByteArrayInputStream)
           (java.net.http HttpResponse$BodyHandlers)
           (java.nio ByteBuffer)
           (java.nio.channels Channels)
           (java.nio.charset StandardCharsets)))

(set! *warn-on-reflection* true)

(comment
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.jetty/create-connector"))

(deftest ok-body
  (is (= "ok"
        (-> (constantly {:body   "ok"
                         :status 200})
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          :body))))

(deftest all-writable-body
  (is (= "ok"
        (-> (constantly {:body   (fn [output-stream]
                                   (with-open [w (io/writer output-stream)]
                                     (.write w "ok")))
                         :status 200})
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          :body)))
  (is (= {:status 200 :body ""}
        (-> (constantly {:body   nil
                         :status 200})
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          (select-keys [:status :body]))))
  (is (= "ok"
        (-> (constantly {:body   (.getBytes "ok" StandardCharsets/UTF_8)
                         :status 200})
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          :body)))
  (is (= "ok"
        (-> (constantly {:body   (io/input-stream (.getBytes "ok" StandardCharsets/UTF_8))
                         :status 200})
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          :body)))
  (is (= "ok"
        (-> (constantly {:body   (ByteBuffer/wrap (.getBytes "ok" StandardCharsets/UTF_8))
                         :status 200})
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          :body)))
  (is (= "ok"
        (-> (constantly {:body   (Channels/newChannel
                                   (ByteArrayInputStream. (.getBytes "ok" StandardCharsets/UTF_8)))
                         :status 200})
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          :body)))
  (is (= "{:ok 42}"
        (-> (constantly {:body   {:ok 42}
                         :status 200})
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          :body))))

(defn async-write-body
  [body]
  (interceptor/interceptor
    {:name  :async
     :enter (fn [ctx]
              (async/go
                (assoc ctx :response {:body   body
                                      :status 200})))}))

(deftest all-async-writable-body
  (is (= "ok"
        (-> (async-write-body (fn [output-stream]
                                (with-open [w (io/writer output-stream)]
                                  (.write w "ok"))))
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          :body)))
  (is (= {:status 200 :body ""}
        (-> (async-write-body nil)
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          (select-keys [:status :body]))))
  (is (= "ok"
        (-> (async-write-body (.getBytes "ok" StandardCharsets/UTF_8))
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          :body)))
  (is (= "ok"
        (-> (async-write-body (io/input-stream (.getBytes "ok" StandardCharsets/UTF_8)))
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          :body)))
  (is (= "ok"
        (-> (async-write-body (ByteBuffer/wrap (.getBytes "ok" StandardCharsets/UTF_8)))
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          :body)))
  (is (= "ok"
        (-> (async-write-body (Channels/newChannel
                                (ByteArrayInputStream. (.getBytes "ok" StandardCharsets/UTF_8))))
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          :body)))
  (is (= "{:ok 42}"
        (-> (async-write-body {:ok 42})
          (api/simple-request {} (HttpResponse$BodyHandlers/ofString))
          :body))))
