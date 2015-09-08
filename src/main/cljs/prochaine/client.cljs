(ns prochaine.client
  "prochaine: om.next technical study"
  ;; (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [goog.object :as object]
            ;; [cljs-http.client :as http]
            ;; [cljs.core.async :as async :refer [<! >! chan]]
            [cljs.pprint :refer [pprint]]
            [om.next :as om :refer-macros [defui]]
            [prochaine.next :as tom]
            [om.next.protocols :as p]
            [om.next.stores :refer [TreeStore]]
            [om.dom :as dom]
            [datascript :as d]
            [prochaine.ds :as ds]))

;; Development notes
;;(use 'figwheel-sidecar.repl-api)
;;(cljs-repl)
;;(in-ns 'prochaine.client)

(enable-console-print!)

(defonce app-state (atom nil))
(defonce reconciler (atom nil))

(def bob-martha
  {:app/contacts
   [{:person/first-name "Bob",
     :person/last-name "Smith"
     :person/telephone [{:telephone/number "111-111-1111"}]
     :person/address
     [{:address/street "Maple Street",
       :address/city "Boston",
       :address/state "Massachusetts",
       :address/zipcode "11111"}]}
    {:person/first-name "Martha",
     :person/last-name "Smith"
     :person/telephone [{:telephone/number "111-111-1112"}]
     :person/address
     [{:address/street "Maple Street",
       :address/city "Boston",
       :address/state "Massachusetts",
       :address/zipcode "11112"}]}]})

(def tom
  {:app/contacts
   [{:person/first-name "Tom",
     :person/last-name "Marble",
     :person/telephone [{:telephone/number "111-111-9999"}],
     :person/address [{:address/street "Main St.",
                       :address/city "Edina",
                       :address/state "Minnesota",
                       :address/zipcode "55438"}]}
    ]})

(defn fetch-contacts
  "Return simple, static contacts data (ignore the query)."
  [query]
  bob-martha)

(defn add-random-contact []
  (let [contact (rand-nth (:app/contacts (merge-with concat bob-martha tom)))]
    (swap! app-state #(p/push % (-> %
                                    (p/pull [:app/contacts] nil)
                                    :app/contacts
                                    (conj contact))
                              [:app/contacts]))))

(defn label+span
  "Construct a label and span (with optional opts)."
  ([label-content span-content]
   (label+span nil label-content span-content))
  ([opts label-content span-content]
   (let [label-content (if-not (sequential? label-content)
                        [label-content]
                        label-content)
         span-content (if-not (sequential? span-content)
                        [span-content]
                        span-content)]
     (dom/div opts
       (apply dom/label nil label-content)
       (apply dom/span nil span-content)))))

(defn load-all-data! [conn]
  (println "Loading data...")
  (d/transact! conn
               [{:db/id -1
                 :app/title "Hello, World!"
                 :app/state [{:db/id -1 :state/count 0}
                             {:db/id -2 :state/count 0}
                             {:db/id -3 :state/count 0}]}]))

;; ============================================================
;; AddressInfo Component

(defui AddressInfo
  static om/IQuery
  (-query [this]
    '[:address/street :address/city :address/zipcode])
  Object
  (render [this]
    (let [{:keys [:address/street :address/city
                  :address/state :address/zipcode]}
          (om/props this)]
      (label+span #js {:className "address"}
        "Address:"
        (dom/span nil
          (str street " " city ", " state " " zipcode))))))

(def address-info (om/create-factory AddressInfo))

;; ============================================================
;; Contact Component

(defui Contact
  static om/IQueryParams
  (-params [this]
    {:address (om/query AddressInfo)})
  static om/IQuery
  (-query [this]
    '[:person/first-name :person/last-name
      {:person/telephone [:telephone/number]}
      {:person/address ?address}])
  Object
  (render [this]
    (let [{:keys [:person/first-name :person/last-name
                  :person/address] :as props}
          (om/props this)]
      (dom/div nil
        (label+span "Full Name:"
          (str last-name ", " first-name))
        (label+span "Number:"
          (:telephone/number (first (:person/telephone props))))
        (address-info (first address))))))

(def contact (om/create-factory Contact))

;; ============================================================
;; ContactList Component

(defui ContactList
  static om/IQueryParams
  (-params [this]
    {:contact (om/query Contact)})
  static om/IQuery
  (-query [this]
    '[{:app/contacts ?contact}])
  Object
  (render [this]
    (let [{:keys [:app/contacts]} (om/props this)]
      (dom/div nil
        (dom/h3 nil "Contacts")
        (dom/div nil
                 (dom/button #js {:onClick add-random-contact} "Add Random Person"))
        (apply dom/ul nil
               (map #(dom/li nil (contact %)) contacts))))))

(def contact-list (om/create-factory ContactList))

(def store (atom (ds/DataScriptStore. @ds/conn ds/conn nil nil)))

;; ============================================================
;; main

;; (defn main []
;;   (let [c (fetch (om/query ContactList))]
;;     (go
;;       (let [contacts (:body (<! c))]
;;         (js/React.render
;;           (contact-list contacts)
;;           (gdom/getElement "demo3"))))))

(defn main []
  (println "-- main --")
  (load-all-data! ds/conn)
  (let [query (om/query ContactList)
        contacts (fetch-contacts query)
        app (gdom/getElement "app")]
    (reset! app-state (TreeStore. contacts))
    ;; (reset! reconciler (om/tree-reconciler app-state))
    (reset! reconciler (tom/tree-reconciler app-state))
    (om/add-root! @reconciler app ContactList)))

(when (gdom/getElement "app")
  (main))

;; trying to update the app-state by pushing to the store
;; *should* provoke an update, but does not because
;; even though the tree-reconciler is watching the app-state atom
;; because the render function isn't given a data argument at
;; https://github.com/omcljs/om/blob/master/src/om/next.cljs#L385
;;
;; modified in prochaine.next.cljs to grab root data (if not provided)

;; (swap! app-state #(p/push % tom nil))
;; (swap! app-state #(p/push % bob-martha nil))
