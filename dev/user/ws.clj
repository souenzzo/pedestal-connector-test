(ns user.ws
  (:require [clojure.core.async :as async]
            [io.pedestal.connector :as conn]
            [io.pedestal.log :as log]
            [io.pedestal.service.websocket :as websocket]
            [pedestal-connector-test.api :as api])
  (:import (java.net URI)
           (java.net.http HttpClient WebSocket WebSocket$Listener)))

(set! *warn-on-reflection* true)

(defn -main
  [& _]
  (let [*evts (atom {})
        conn (-> (conn/default-connector-map 8080)
               (conn/with-interceptor
                 (websocket/websocket-interceptor ::ws
                   {:on-close  (fn on-close [channel proc reason]
                                 (swap! *evts conj {:on-text {:channel channel
                                                              :proc    proc
                                                              :reason  reason}}))
                    :on-binary (fn on-binary [channel proc buffer]
                                 (swap! *evts conj {:on-binary {:channel channel
                                                                :proc    proc
                                                                :buffer  buffer}}))
                    :on-open   (fn [channel ring-request]
                                 (swap! *evts conj {:on-open {:channel channel :ring-request ring-request}})
                                 (websocket/start-ws-connection channel nil))
                    :on-text   (fn [channel proc text]
                                 (swap! *evts conj {:on-text {:channel channel
                                                              :proc    proc
                                                              :text    text}}))}))
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
          (Thread/sleep 100)
          (.thenRun (.sendClose ws WebSocket/NORMAL_CLOSURE "Fim")
            (fn []
              (deliver done :ok)))
          (deref done 1000 :timeout)))
      (finally
        (conn/stop! conn)))))

(comment

  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.jetty/create-connector")
  (-main))

