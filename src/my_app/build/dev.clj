(ns my-app.build.dev
  (:require
   [adzerk.boot-cljs-repl :as boot-cljs-repl]
   [clojure.pprint :as clj-pprint :refer [pprint]]
   [com.stuartsierra.component :as component]
   [my-app.backend.core :as my-app-system]
   [my-app.build
    [css :as css]
    [fix :as fix]
    [time-reporting :as time-reporting]]))

(defonce system nil)

(defn show-system []
  (clj-pprint/pprint system))

(defn- init
  []
  (alter-var-root
   #'system
   (constantly (my-app-system/system :development))))

(defn- start []
  (println "Attempting to start the system...")
  (init)
  (css/build :development)
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root
   #'system
   (fn [system]
     (println "Attempting to stop the system...")
     (show-system)
     (if system
       (try (component/stop system)
            nil
            (catch Throwable t
              (prn t)
              system))
       (println "System already stopped!")))))

(defn go []
  (if system
    (println "The system is already running!")
    (do
      (start)
      (show-system))))

(defn reset []
  (stop)
  (let [ret (fix/refresh {:function-to-run-after-refresh 'my-app.build.dev/go})]
    (if (instance? Throwable ret)
      (throw ret)  ;; Let the REPL's exception handling take over
      ret)))

(defn safe-refresh
  "It's important that we stop the component before refreshing all the
  namespaces because otherwise the server will still be running in the
  background and we'll have lost the reference. This will then cause
  a resource in use error when we want to start the component up again"
  ([refresh-all?]
   (let [started-at (time-reporting/get-time-in-ms-now)]
     (stop)
     (fix/refresh {:refresh-all? refresh-all?})
     (time-reporting/measure-and-report-elapsed-time
      "Reloaded namespaces after: "
      started-at))))

(defn start-cljs []
  (boot-cljs-repl/start-repl))
