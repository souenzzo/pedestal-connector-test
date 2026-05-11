(ns pedestal-connector-test.request.uri-test
  (:require [clojure.test :refer [deftest is]]
            [pedestal-connector-test.api :as api])
  (:import (java.text Normalizer Normalizer$Form)))

(comment
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.http-kit/create-connector")
  (System/setProperty "pedestal-connector-test.api/create-connector" "io.pedestal.http.jetty/create-connector")
  (System/setProperty "pedestal-connector-test.api/port" "8080"))

(deftest hello-uri
  (is (= "/hello"
        (-> {:uri "/hello"}
          api/capture-request
          :uri))))

(deftest edge-values
  (is (= "/"
        (-> {:uri nil}
          api/capture-request
          :uri)))
  (is (= "/%20"
        (-> {:uri "/ "}
          api/capture-request
          :uri)))
  (is (= "/%22"
        (-> {:uri "/\""}
          api/capture-request
          :uri)))
  (is (= "/%23"
        (-> {:uri "/#"}
          api/capture-request
          :uri)))
  (is (= "/%C2%AC"
        (-> {:uri "/¬"}
          api/capture-request
          :uri)))
  (is (= "/!$&'()*+,-."
        (-> {:uri "/!$&'()*+,-."}
          api/capture-request
          :uri)))
  (is (= "/%C3%A3"
        (-> {:uri "/ã"}
          api/capture-request
          :uri)))
  (is (= "/%F0%9F%A6%84"
        (-> {:uri "/🦄"}
          api/capture-request
          :uri)))
  #_(is (= "/%"
          (-> {:uri "/%20"}
            api/capture-request
            :uri))))


(deftest text-normalization
  (let [text "/🦄çã"]
    (is (= "/%F0%9F%A6%84%C3%A7%C3%A3"
          (-> {:uri text}
            api/capture-request
            :uri)))
    (is (= "/%F0%9F%A6%84%C3%A7%C3%A3"
          (-> {:uri (Normalizer/normalize text Normalizer$Form/NFD)}
            api/capture-request
            :uri)))
    (is (= "/%F0%9F%A6%84%C3%A7%C3%A3"
          (-> {:uri (Normalizer/normalize text Normalizer$Form/NFC)}
            api/capture-request
            :uri)))
    (is (= "/%F0%9F%A6%84%C3%A7%C3%A3"
          (-> {:uri (Normalizer/normalize text Normalizer$Form/NFKD)}
            api/capture-request
            :uri)))
    (is (= "/%F0%9F%A6%84%C3%A7%C3%A3"
          (-> {:uri (Normalizer/normalize text Normalizer$Form/NFKC)}
            api/capture-request
            :uri)))))
