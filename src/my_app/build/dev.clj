(ns my-app.build.dev
  (:require
   [adzerk.boot-cljs-repl :as boot-cljs-repl]
   [clojure.pprint :as clj-pprint :refer [pprint]]
   [com.stuartsierra.component :as component]
   [my-app.backend.core :as my-app]
   [my-app.build.fix :as fix]
   [my-app.build.frontend :as frontend]
   [my-app.build.idiomatic :as idiomatic]
   [my-app.build.lint :as lint]
   [my-app.build.test :as tester]
   ))

(def system nil)

(defn show-system []
  (clj-pprint/pprint system))

(defn init
  []
  (alter-var-root
   #'system
   (constantly (my-app/system :development))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root
   #'system
   (fn [system] (when system (component/stop system)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (fix/refresh 'my-app.build.dev/go))

(defn safe-refresh
  "It's important that we stop the component before refreshing all the
  namespaces because otherwise the server will still be running in the
  background and we'll have lost the reference. This will then cause
  a resource in use error when we want to start the component up again"
  []
  (stop)
  (fix/refresh))

(defn analyse []
  (idiomatic/analyse))

(defn check []
  (lint/lint)
  (tester/run-tests))

(defn t []
  (safe-refresh)
  (frontend/build-cljs)
  (check)
  ;; TODO: run frontend unit tests here
  )

(defn start-cljs []
  (boot-cljs-repl/start-repl))
