(ns pedestal-connector-test.websocket.on-open-test
  (:require [clojure.test :refer [deftest is]]
            [io.pedestal.connector :as conn]
            [io.pedestal.service.websocket :as websocket]
            [pedestal-connector-test.api :as api])
  (:import (java.net URI)
           (java.net.http HttpClient WebSocket WebSocket$Listener)))

(comment
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.jetty/create-connector"))

(set! *warn-on-reflection* true)

(defn capture-ws-events
  [ws-opts]
  (let [*evts (atom [])
        on-evt (fn [evt]
                 (fn [& args]
                   (swap! *evts conj evt)
                   (let [impl (get ws-opts evt (constantly nil))]
                     (apply impl args))))
        conn (-> (conn/default-connector-map 8080)
               (conn/with-interceptor
                 (websocket/websocket-interceptor ::ws
                   {:on-close  (on-evt :on-close)
                    :on-binary (on-evt :on-binary)
                    :on-open   (on-evt :on-open)
                    :on-text   (on-evt :on-text)}))
               (api/create-connector nil))]
    (try
      (conn/start! conn)
      (with-open [http-client (HttpClient/newHttpClient)]
        (let [listener (reify WebSocket$Listener)
              ^WebSocket ws (-> http-client
                              .newWebSocketBuilder
                              (.buildAsync (URI/create "ws://0:8080/")
                                listener)
                              .join)
              done (promise)]
          (.thenRun (.sendClose ws WebSocket/NORMAL_CLOSURE "Fim")
            (fn []
              (deliver done :ok)))
          (.shutdownNow http-client)
          (deref done 1000 :timeout)))
      (finally
        (conn/stop! conn)))
    @*evts))

(deftest on-open
  (let [*args (promise)
        evts (capture-ws-events {:on-open (fn [& args]
                                            (deliver *args args))})]
    (is (= [:on-open :on-close]
          evts))
    (is (realized? *args))
    (let [[channel ring-request] (deref *args 100 [])]
      (is (satisfies? websocket/WebSocketChannel channel))
      (is (every? (set (keys ring-request))
            #{:body :headers #_:protocol :query-string :remote-addr :request-method :scheme :server-name :server-port
              #_:ssl-client-cert :uri})))))

