(ns dpisarenko.wn2-prj
  (:require [dpisarenko.process.v2.entry-point
             :refer [writing-process-v2]])
  (:require [dpisarenko.works.wn2.entry-point
            :refer [wn2-data]])
  )

(def data
  {
    :process writing-process-v2
    :work wn2-data
   })