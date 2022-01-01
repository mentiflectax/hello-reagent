(ns hello-reagent.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [reagent.dom :as rdom]
    [reagent.session :as session]
    [reitit.frontend :as reitit]
    [clerk.core :as clerk]
    [accountant.core :as accountant]
    [dpisarenko.wn2-prj :refer [wn2-project-data]]
    )
  )

;; -------------------------
;; Routes

(def router
  (reitit/router
    [["/" :index]
     ["/items"
      ["" :items]
      ["/:item-id" :item]]
     ["/about" :about]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

;; -------------------------
;; Page components

(defn home-page []
  (fn []
    (let [
          work (:work wn2-project-data)
          working-title (:working-title work)]
      [:span.main
       [:h1 (str working-title)]
       [:ul
        [:li [:a {:href (path-for :items)} "Items of hello-reagent"]]
        ; [:li [:a {:href "/broken/link"} "Broken link"]]

        ]]
      )
    ))



(defn items-page []
  (fn []
    [:span.main
     [:h1 "The items of hello-reagent"]
     [:ul (map (fn [item-id]
                 [:li {:name (str "item-" item-id) :key (str "item-" item-id)}
                  [:a {:href (path-for :item {:item-id item-id})} "Item: " item-id]])
               (range 1 60))]]))


(defn item-page []
  (fn []
    (let [routing-data (session/get :route)
          item (get-in routing-data [:route-params :item-id])]
      [:span.main
       [:h1 (str "Item " item " of hello-reagent")]
       [:p [:a {:href (path-for :items)} "Back to the list of items"]]])))


(defn about-page []
  (fn [] [:span.main
          [:h1 "Writing Process Template"]
          [:p "I will use this project to improve the novel-writing process."]
          ]))


;; -------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    :index #'home-page
    :about #'about-page
    :items #'items-page
    :item #'item-page))


;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))
          current-year 2022
          ]
      [:div
       [:header
        ; Menu is here
        [:p [:a {:href (path-for :index)} "Home"] " | "
         [:a {:href (path-for :about)} "About hello-reagent"]]]
       [page]
       [:footer
        [:p "Copyright 2021--" current-year " (C) "
         [:a {:href "https://www.amazon.com/author/dpisarenko"}
          "Dmitrii Pisarenko"]
         ]
        ]])))

;; -------------------------
;; Initialize app

(defn mount-root []
  (rdom/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (let [match (reitit/match-by-path router path)
             current-page (:name (:data match))
             route-params (:path-params match)]
         (reagent/after-render clerk/after-render!)
         (session/put! :route {:current-page (page-for current-page)
                               :route-params route-params})
         (clerk/navigate-page! path)
         ))
     :path-exists?
     (fn [path]
       (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))
