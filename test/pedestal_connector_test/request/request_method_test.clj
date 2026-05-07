(ns pedestal-connector-test.request.request-method-test
  (:require [clojure.test :refer [deftest are]]
            [pedestal-connector-test.api :as api]))

;; https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Methods
(deftest every-method
  (are [request-method] (= request-method
                          (-> {:request-method request-method}
                            api/capture-request
                            :request-method))
    :get :head :post :delete #_:connect :options :trace :patch
    #__))
