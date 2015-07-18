(ns contacts.demo3
  "om.next demo3: using static data"
  ;; (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [goog.object :as object]
            ;; [cljs-http.client :as http]
            ;; [cljs.core.async :as async :refer [<! >! chan]]
            [cljs.pprint :refer [pprint]]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

;; (defn log [x]
;;   (println) ;; flush past prompt
;;   (pprint x))

;; Example HTTP POST to retreive data (requires cljs-http)
;; (defn fetch [q]
;;   (http/post "http://localhost:8081/query" {:transit-params q}))

(defn fetch-contacts
  "Return simple, static contacts data (ignore the query)."
  [query]
  {:app/contacts
   [{:person/first-name "Bob",
     :person/last-name "Smith"
     :person/telephone [{:telephone/number "111-111-1111"}]
     :person/address [{:address/street "Maple Street",
                       :address/city "Boston",
                       :address/state "Massachusetts",
                       :address/zipcode "11111"}]}
    {:person/first-name "Martha",
     :person/last-name "Smith"
     :person/telephone [{:telephone/number "111-111-1112"}]
     :person/address [{:address/street "Maple Street",
                       :address/city "Boston",
                       :address/state "Massachusetts",
                       :address/zipcode "11112"}]}]})

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

;; ============================================================
;; AddressInfo Component

(defui AddressInfo
  static om/IQuery
  (query [this]
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
  (params [this]
    {:address (om/get-query AddressInfo)})
  static om/IQuery
  (query [this]
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
  (params [this]
    {:contact (om/get-query Contact)})
  static om/IQuery
  (query [this]
    '[{:app/contacts ?contact}])
  Object
  (render [this]
    (let [{:keys [:app/contacts]} (om/props this)]
      (dom/div nil
        (dom/h3 nil "Contacts")
        (apply dom/ul nil
          (map #(dom/li nil (contact %)) contacts))))))

(def contact-list (om/create-factory ContactList))

;; ============================================================
;; main

;; (defn main []
;;   (let [c (fetch (om/get-query ContactList))]
;;     (go
;;       (let [contacts (:body (<! c))]
;;         (js/React.render
;;           (contact-list contacts)
;;           (gdom/getElement "demo3"))))))

(defn main []
  (let [query (om/get-query ContactList)
        contacts (fetch-contacts query)]
    (js/React.render
      (contact-list contacts)
      (gdom/getElement "app"))))

(when (gdom/getElement "app")
  (main))

;; things to try on the REPL...

;; contacts.demo3=> (pprint (om/get-query AddressInfo))
;; [:address/street :address/city :address/zipcode]

;; contacts.demo3=> (pprint (om/get-query Contact))
;; [:person/first-name
;;  :person/last-name
;;  {:person/telephone [:telephone/number]}
;;  {:person/address [:address/street :address/city :address/zipcode]}]

;; contacts.demo3=> (pprint (om/get-query ContactList))
;; [{:app/contacts
;;   [:person/first-name
;;    :person/last-name
;;    {:person/telephone [:telephone/number]}
;;    {:person/address [:address/street :address/city :address/zipcode]}]}]
