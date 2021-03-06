(ns my-app.backend.smoke-test
  (:require
   [clj-http.client :as clj-http-client]
   [clojure.string :as string]
   [clojure.test :as test]
   [my-app.backend.config :as config]
   [my-app.backend.test-automation-system :as test-automation-system]
   ))

(defn fixture
  [f]
  (test-automation-system/init :test-automation)
  (test-automation-system/start)
  (f)
  (test-automation-system/stop))

(test/use-fixtures :once fixture)

(defn- assert-route-provides-document-with-text
  [route text-to-find]
  (let [config (config/get-config-for :test-automation)
        server-response (clj-http-client/get
                         (str
                          (config/get-fully-qualified-url config)
                          route)
                         {:throw-exceptions false})
        response-body (:body server-response)]
    (test/is (string/includes? response-body text-to-find))))

(test/deftest core-smoke-test
  (test/testing "Gets homepage at base route"
    (let [base-route ""
          text-to-find "This is my test app smoke marker"]
      (assert-route-provides-document-with-text base-route text-to-find)))

  #_(test/testing "Gets 404 page at non-existant route"
      (let [non-existant-route "foobar"
            text-to-find "404"]
        (assert-route-provides-document-with-text
         non-existant-route
         text-to-find)))

  (test/testing "Gets static resource js"
    (let [resource-route "js/my_app.js"
          config (config/get-config-for :test-automation)
          response (clj-http-client/get
                    (str
                     (config/get-fully-qualified-url config)
                     resource-route)
                    {:throw-exceptions false})]
      (test/is (= 200
                  (get response :status)))))

  (test/testing "Gets static resource image"
    (let [resource-route "clojure_logo.svg.png"
          config (config/get-config-for :test-automation)
          response (clj-http-client/get
                    (str
                     (config/get-fully-qualified-url config)
                     resource-route)
                    {:throw-exceptions false})]
      (test/is (= 200
                  (get response :status))))))
