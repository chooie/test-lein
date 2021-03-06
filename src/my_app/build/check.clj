(ns my-app.build.check
  (:require
   [my-app.build
    [backend-tester :as backend-tester]
    [css :as css]
    [external-dependencies :as my-app-external-dependencies]
    [frontend :as frontend]
    [idiomatic :as idiomatic]
    [lint :as lint]
    [time-reporting :as time-reporting]]
   [my-app.smoke-test :as smoke]))

(defn analyse []
  (idiomatic/analyse))

(defn run-smoke-tests []
  (let [started-at (time-reporting/get-time-in-ms-now)]
    (println "Running smoke tests...")
    (smoke/run-tests)
    (time-reporting/measure-and-report-elapsed-time
     "Ran smoke tests after: "
     started-at)))

(defn full-backend-check []
  (my-app-external-dependencies/check-java-version)
  (backend-tester/run-tests))

(defn lint-and-full-backend-check []
  (lint/lint-backend)
  (full-backend-check))

(defn frontend-check []
  ;; Don't include this in backend check as we're not running this in prod
  (my-app-external-dependencies/check-node-process-version)
  (when (or
         (frontend/frontend-tests-failed-last?)
         (frontend/frontend-contents-changed?))
    (css/build :test-automation)
    (frontend/build-cljs)
    (frontend/run-tests-with-karma)))

(defn run-all-unit-tests []
  (frontend-check)
  (full-backend-check))

(defn lint-and-run-all-unit-tests []
  (lint/lint-backend)
  (run-all-unit-tests))
