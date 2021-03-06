(ns mailer.message
  (:require [hiccup.core :refer [html]]))

(defn msg
  [& content]
  (html
   [:body content]))

(defn greet [name]
  (html [:p (format "Hi %s," name)]))

(defn p [& text]
  (html [:p text]))

(defn sig
  ([]
   (sig "The Starcity Team"))
  ([name]
   (html [:p "Thanks, " [:br] name]))
  ([name title]
   (html [:p "Best," [:br] [:br] name [:br] title])))
