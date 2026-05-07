(ns pedestal-connector-test.response.status-test
  (:require [clojure.test :refer [deftest is are]]
            [pedestal-connector-test.api :as api]))

(set! *warn-on-reflection* true)

(comment
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.jetty/create-connector"))

(deftest status-204
  (is (= 204
        (-> (constantly {:status 204})
          (api/simple-request {})
          :status))))

;; https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status
(deftest every-status
  (are [status] (= status
                  (-> (constantly {:status status})
                    (api/simple-request {})
                    :status))
                ;; 100 101 102 103 199
                200 201 202 203 204 205 206 207 208 226 299
                300 301 302 303 304 305 306 307 308 399
                400 401 402 403 404 405 406 407 408 409
                410 411 412 413 414 415 416 417 418
                421 422 423 424 425 426 428 429
                431 451 499
                500 501 502 503 504 505 506 507 508 510 511 599
                #__))
