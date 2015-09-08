(ns prochaine.next
  "prochaine: bug fixes? for next.cljs"
  (:require [goog.object :as object]
            [cljs.pprint :refer [pprint]]
            [om.next :as om :refer-macros [defui]]
            [om.next.protocols :as p]
            [om.next.stores :refer [TreeStore]]))

;; NOTE: this is a proposed fix to
;; https://github.com/omcljs/om/blob/master/src/om/next.cljs#L385

(defn tree-reconciler [data]
  (let [state  (cond
                 (satisfies? IAtom data) data
                 (satisfies? p/IStore data) (atom data)
                 (map? data) (atom (TreeStore. data))
                 :else (throw (ex-info "data must be an atom, store, or map"
                                {:type ::invalid-argument})))
        idxs   (atom {})
        queue  (atom [])
        queued (atom false)
        roots  (atom {})
        t      (atom 0)
        r      (reify
                 p/ICommitQueue
                 (commit! [_ next-props component]
                   (let [index (om/index component)
                         path  (cond->
                                 (get-in @idxs
                                   [(om/root-class component)
                                    :component->path (type component)])
                                 index (conj index))]
                     (swap! t inc) ;; TODO: probably should revisit doing this here
                     (swap! queue conj [component next-props])
                     (swap! state p/push next-props path)))
                 p/IReconciler
                 (basis-t [_] @t)
                 (store [_] @state)
                 (indexes [_] @idxs)
                 (props-for [_ component]
                   (let [rc    (om/root-class component)
                         ct    (type component)
                         index (om/index component)
                         state @state
                         path  (cond->
                                 (get-in @idxs [rc :component->path ct])
                                 index (conj index))]
                     (get-in
                       (p/pull state
                         (get-in @idxs [rc :component->selector ct])
                         nil)
                       path)))
                 (add-root! [this target root-class options]
                   (let [ret (atom nil)
                         rctor (om/create-factory root-class)]
                     (swap! idxs assoc root-class (om/build-index root-class))
                     (let [sel     (om/query root-class)
                           renderf (fn [data]
                                     (let [data (or data
                                                  (p/pull @state sel nil))]
                                       (binding [om/*reconciler* this
                                                 om/*root-class* root-class]
                                         ;; (println "render data:" data)
                                         (reset! ret
                                           (js/React.render (rctor data) target)))))
                           store   @state]
                       (swap! roots assoc target renderf)
                       (cond
                         (satisfies? p/IPullAsync store) (p/pull-async store sel nil renderf)
                         :else (renderf (p/pull store sel nil)))
                       @ret)))
                 (remove-root! [_ target]
                   (swap! roots dissoc target))
                 (schedule! [_]
                   (if-not @queued
                     (swap! queued not)
                     false))
                 (reconcile! [_]
                   (if (empty? @queue)
                     (do
                       (doseq [[_ renderf] @roots]
                         ;; (println "reconcile! renderf for: " om/*root-class*)
                         (renderf)))
                     (do
                       (doseq [[component next-props]
                               (sort-by (comp om/depth first) @queue)]
                         (when (om/should-update? component next-props)
                           (om/update-component! component next-props)))
                       (reset! queue [])))
                   (swap! queued not)))]
    (add-watch state :om/simple-reconciler
      (fn [_ _ _ _] (om/schedule! r)))
    r))
