(ns pedestal-connector-test.api
  (:require [clojure.string :as string]
            [io.pedestal.connector :as conn]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.service.websocket :as websocket])
  (:import (java.lang AutoCloseable)
           (java.net URI)
           (java.net.http HttpClient HttpClient$Version HttpHeaders HttpRequest HttpResponse$BodyHandlers WebSocket
                          WebSocket$Listener)
           (java.time Duration)
           (java.util Optional)))

(set! *warn-on-reflection* true)

(comment
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/port" "8080"))

(def connector-ident
  (let [create-connector (System/getProperty "pedestal-connector-test.api/create-connector")]
    (case create-connector
      "io.pedestal.http.http-kit/create-connector" :http-kit
      "io.pedestal.http.jetty/create-connector" :jetty
      create-connector)))

(defn create-connector
  [& argv]
  (let [create-connector-str (System/getProperty "pedestal-connector-test.api/create-connector")

        create-connector (some-> create-connector-str
                           symbol
                           requiring-resolve
                           deref)]
    (when-not (fn? create-connector)
      (throw (ex-info "Can't find create-connector" {:property create-connector-str})))
    (apply create-connector argv)))

(defn start-stop-interceptor
  [interceptor
   {:keys [request-method headers uri query-string body protocol]
    :or   {request-method :get
           uri            "/"
           headers        {}}
    :as   ring-request}
   response-body-handler]
  (let [port (Long/getLong "pedestal-connector-test.api/port" 1337)
        conn (-> port
               conn/default-connector-map
               (conn/with-interceptor interceptor)
               (create-connector {})
               conn/start!)]
    (try
      (let [http-client (HttpClient/newHttpClient)]
        (try
          (let [http-response (.send http-client
                                (proxy [HttpRequest] []
                                  (method [] (-> request-method name string/upper-case))
                                  (timeout [] (Optional/of (Duration/ofSeconds 1)))
                                  (expectContinue [] false)
                                  (bodyPublisher [] (if (contains? ring-request :body)
                                                      (Optional/of body)
                                                      (Optional/empty)))
                                  (version [] (if (contains? ring-request :protocol)
                                                (Optional/of
                                                  (case protocol
                                                    "HTTP/1.1" HttpClient$Version/HTTP_1_1
                                                    "HTTP/2.0" HttpClient$Version/HTTP_2))
                                                (Optional/empty)))
                                  (headers [] (HttpHeaders/of headers
                                                (constantly true)))
                                  (uri [] (URI. "http" nil "0" port uri query-string nil)))
                                response-body-handler)]
            {:body    (.body http-response)
             :headers (into {}
                        (map (fn [[k vs]]
                               [k (if (next vs)
                                    (vec vs)
                                    (first vs))]))
                        (.map (.headers http-response)))
             :status  (.statusCode http-response)})
          (finally
            (when (instance? AutoCloseable http-client)
              (.close ^AutoCloseable http-client)))))
      (finally
        (conn/stop! conn)))))

(defn simple-request
  ([ring-handler ring-request]
   (simple-request ring-handler ring-request (HttpResponse$BodyHandlers/discarding)))
  ([ring-handler ring-request response-body-handler]
   (start-stop-interceptor
     (if (interceptor/interceptor? ring-handler)
       ring-handler
       {:name  :ring-handler
        :enter (fn [{:keys [request]
                     :as   ctx}]
                 (assoc ctx :response (ring-handler request)))})
     ring-request
     response-body-handler)))

(defn capture-request
  ([ring-request]
   (capture-request ring-request identity))
  ([ring-request on-body]
   (let [*request (promise)
         {:keys [status]
          :as ring-response} (start-stop-interceptor
                               {:name  :ring-handler
                                :enter (fn [{:keys [request]
                                             :as   ctx}]
                                         (deliver *request (if (contains? request :body)
                                                             (update request :body on-body)
                                                             request))
                                         (assoc ctx :response {:status 204}))}
                               ring-request
                               (HttpResponse$BodyHandlers/ofString))]
     (when-not (== status 204)
       (throw (ex-info "Unexpected return" ring-response)))
     (if (realized? *request)
       @*request
       (throw (ex-info "Unrealized request" {}))))))


(defn capture-ws-events
  [ws-opts & ops]
  (let [port (Long/getLong "pedestal-connector-test.api/port" 1337)
        *evts (atom [])
        on-evt (fn [evt]
                 (fn [& args]
                   (swap! *evts conj evt)
                   (let [impl (get ws-opts evt (constantly nil))]
                     (apply impl args))))
        conn (-> (conn/default-connector-map port)
               (conn/with-routes
                 #{["/" :get (websocket/websocket-interceptor ::ws
                               {:on-close  (on-evt :on-close)
                                :on-binary (on-evt :on-binary)
                                :on-open   (on-evt :on-open)
                                :on-text   (on-evt :on-text)})]})
               (create-connector nil))]
    (try
      (conn/start! conn)
      (let [http-client (HttpClient/newHttpClient)]
        (try
          (let [listener (reify WebSocket$Listener)
                ^WebSocket ws (-> http-client
                                .newWebSocketBuilder
                                (.buildAsync (URI/create (str "ws://0:" port))
                                  listener)
                                .join)
                done (promise)]
            (doseq [op ops]
              (op ws))
            (.thenRun (.sendClose ws WebSocket/NORMAL_CLOSURE "Fim")
              (fn []
                (deliver done :ok)))
            (.shutdownNow http-client)
            (deref done 1000 :timeout))
          (when (instance? AutoCloseable http-client)
            (.close http-client))))
      (finally
        (conn/stop! conn)))
    @*evts))


(defonce *dev-local (atom nil))

(defn start!
  []
  (swap! *dev-local
    (fn [conn]
      (some-> conn conn/stop!)
      (-> 8888
        (conn/default-connector-map)
        (conn/with-interceptor
          {:name :handler
           :enter (fn [ctx]
                    (def _ctx ctx)
                    (assoc ctx :response {:status 204}))})
        (create-connector {})
        conn/start!))))

(defn stop!
  []
  (swap! *dev-local (fn [conn]
                      (some-> conn conn/stop!))))

(comment
  (start!)
  (stop!)
  (-> _ctx
    :request
    :headers))
