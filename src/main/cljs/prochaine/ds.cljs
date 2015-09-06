(ns prochaine.ds
  (:require [datascript :as d]))

(def schema
  #_{:app/contacts {:db/valueType :db.type/ref
                  :db/cardinality :db.cardinality/many
                  :db/isComponent true}}
  {:aka {:db/cardinality :db.cardinality/many}})

(def conn (d/create-conn schema))

