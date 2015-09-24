(ns prochaine.ds
  (:require [datascript :as d]
            [om.next :as om]
            [om.next.protocols :as p]))

(def schema
  {:app/contacts {:db/valueType :db.type/ref
                  :db/cardinality :db.cardinality/many
                  :db/isComponent true}})

(def conn (d/create-conn schema))

(defmulti ds-query (fn [_ root _] root))

(defmethod ds-query :app/title
  [db _ selector]
  ;; make this query work
  (d/q '[:find (pull ?e ?selector)
         :in $ ?selector
         :where [?e :app/title]]
    db (conj selector :db/id)))

(comment
  ;; make this query work
  (ds-query db :app/title selector)
  )

(deftype DataScriptStore [db conn tx-report index]
  p/IPull
  (pull [this selector ctxt]
    (ds-query db :app/title selector)
    #_(let [[root root-selector] (first selector)]
      (ds-query db root root-selector)))
  ;;p/IPush
  #_(push [this tx-data _]
    (DataScriptStore. @conn conn (d/transact! conn [entity]) (atom @index)))
  ;;p/IStore
  #_(commit [this tx-data component]
    (let [{:keys [tx-report] :as next} (p/push this tx-data nil)]
      (when component
        (om/update-props component (db/entity db (-> component om/props :db/id)))
        ;; TODO: grab component and update its props - David
        [next component])))
  ;;p/IComponentIndex
  #_(index-component [this component]
    (swap! index assoc (:db/id (om/props component)) component))
  #_(drop-component [this component]
    (swap! index dissoc (:db/id (om/props component)))))

(defn increment! [c props]
  (println props)
  #_(om/commit! c (update-in props [:state/count] inc)))
