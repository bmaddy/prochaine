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

(def bob-data
  {:person/first-name "Bob",
     :person/last-name "Smith"
     :person/telephone [{:telephone/number "111-111-1111"}]
     :person/address
     [{:address/street "Maple Street",
       :address/city "Boston",
       :address/state "Massachusetts",
       :address/zipcode "11111"}]})

(def martha-data
  {:person/first-name "Martha",
     :person/last-name "Smith"
     :person/telephone [{:telephone/number "111-111-1112"}]
     :person/address
     [{:address/street "Maple Street",
       :address/city "Boston",
       :address/state "Massachusetts",
       :address/zipcode "11112"}]})

(def bob-martha
  {:app/contacts
   [bob-data
    martha-data]})

(def tom-data
  {:person/first-name "Tom",
     :person/last-name "Marble",
     :person/telephone [{:telephone/number "111-111-9999"}],
     :person/address [{:address/street "Main St.",
                       :address/city "Edina",
                       :address/state "Minnesota",
                       :address/zipcode "55438"}]})
(def tom
  {:app/contacts
   [tom-data]})

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

(def schema
  {:app/contacts {:db/valueType :db.type/ref
                  :db/cardinality :db.cardinality/many
                  :db/isComponent true}
   :person/telephone {:db/valueType :db.type/ref
                      :db/cardinality :db.cardinality/many
                      :db/isComponent true}
   :person/address {:db/valueType :db.type/ref
                    :db/cardinality :db.cardinality/many
                    :db/isComponent true}})

(def conn (d/create-conn schema))

#_(def seed-data
  [{:db/id -1
    :app/title "Hello, World!"
    :app/foo "bar"
    :app/state [{:db/id -1 :state/count 0}
                {:db/id -2 :state/count 0}
                {:db/id -3 :state/count 0}]}])

(def seed-data
  [{:app/title "some title here"
    :app/contacts [{:person/first-name "Bob",
                    :person/last-name "Smith"
                    :person/telephone [{:telephone/number "111-111-1111"}
                                       {:telephone/number "222-222-2222"}]
                    :person/address
                    [{:address/street "Maple Street",
                      :address/city "Boston",
                      :address/state "Massachusetts",
                      :address/zipcode "11111"}]}]}])

(defn load-all-data! [conn]
  (println "Loading data...")
  (d/transact! conn
               seed-data))

(comment
  ;; NOTES:
  ;; table-store example
  (om.next.stores/tables-pull
    {:app {:app/title "Hello World!" :app/state [0 1 2]}
     :app/state [{:state/count 0}
                 {:state/count 0}
                 {:state/count 0}]}
    [{:app [:app/title {:app/state [:state/count]}]}])
  {:app {:app/title "Hello World!", :app/state [{:state/count 0} {:state/count 0} {:state/count 0}]}}

  ;; query we want to satisfy:
  (om/query ContactList)
  [{:app/contacts [:person/first-name
                   :person/last-name
                   {:person/telephone [:telephone/number]}
                   {:person/address [:address/street
                                     :address/city
                                     :address/zipcode]}]}]
  ;; expect something like this:
  [{:app/contacts [{:person/first-name "Bob"
                    :person/last-name "Smith"
                    {:person/telephone [{:telephone/number "111-111-1111"}
                                        {:telephone/number "222-222-2222"}]}
                    {:person/address [{:address/street "Maple Street"
                                       :address/city "Boston"
                                       :address/state "Massachusetts"
                                       :address/zipcode "11111"}]}}]}]
  ;; ds query we need to generate:
  (d/q '[:find ])
  )

;; ============================================================
;; AddressInfo Component

(defui AddressInfo
  static om/IQuery
  (-query [this]
          '[:address/street :address/city :address/zipcode]
          ;;'{:app/root [:app/title]}
          )
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

(def store (atom (ds/DataScriptStore. @conn conn nil nil)))

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
  (load-all-data! conn)
  (let [query (om/query ContactList)
        contacts (fetch-contacts query)
        app (gdom/getElement "app")]
    (reset! app-state (TreeStore. contacts))
    ;; (reset! reconciler (om/tree-reconciler app-state))
    ;;(reset! reconciler (tom/tree-reconciler app-state))
    (reset! reconciler (tom/tree-reconciler store))
    ;;(om/add-root! @reconciler app ContactList)
    (om/add-root! @reconciler app AddressInfo)
    ;;(om/add-root! @reconciler app HelloWorld)
    )
  )

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
