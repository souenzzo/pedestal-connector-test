(ns pedestal-connector-test.websocket.on-open-test
  (:require [clojure.test :refer [deftest is]]
            [io.pedestal.service.websocket :as websocket]
            [pedestal-connector-test.api :as api])
  (:import (java.net.http WebSocket)))

(comment
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.jetty/create-connector"))

(set! *warn-on-reflection* true)

(deftest on-open
  (let [*args (promise)
        evts (api/capture-ws-events {:on-open (fn [websocket-channel ring-request]
                                                (deliver *args [websocket-channel
                                                                (dissoc ring-request :servlet-response)]))})]
    (is (= [:on-open :on-close]
          evts))
    (is (realized? *args))
    (let [[websocket-channel ring-request] (deref *args 100 [])]
      (is (satisfies? websocket/WebSocketChannel websocket-channel))
      (is (every? (set (keys ring-request))
            #{:body :headers #_:protocol :query-string :remote-addr :request-method :scheme :server-name :server-port
              #_:ssl-client-cert :uri})))))

(deftest on-open-return-value
  (let [*args (promise)
        evts (api/capture-ws-events {:on-open (fn [_ _]
                                                ::on-open-return)
                                     :on-text (fn [websocket-channel proc text]
                                                (deliver *args [websocket-channel proc text])
                                                nil)}
               (fn [^WebSocket ws]
                 (.sendText ws "Hello" true)
                 (deref *args 100 :errr)))]
    (is (= [:on-open :on-text :on-close]
          evts))
    (is (realized? *args))
    (let [[websocket-channel proc text] (deref *args 100 [])]
      (is (satisfies? websocket/WebSocketChannel websocket-channel))
      (is (= proc ::on-open-return))
      (is (= "Hello" text)))))
