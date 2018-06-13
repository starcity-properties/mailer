(ns mailer.core
  (:refer-clojure :exclude [send])
  (:require [cheshire.core :as json]
            [clojure.core.async :refer [chan put!]]
            [clojure.spec.alpha :as s]
            [mailer.senders :as senders]
            [mailgun.mail :as mail]
            [org.httpkit.client :as http]
            [toolbelt.async :as ta]))

(declare Mailer)

;; =============================================================================
;; Mailgun
;; =============================================================================


(s/def ::uuid uuid?)
(s/def ::from string?)


(defn- send-mail-async
  "Send email to mailgun with the passed creds and the content, *but async*.

  A sample request would look like:
  (send-mail {:key \"key-3ax6xnjp29jd6fds4gc373sgvjxteol1\" :domain \"bar.com\"}
             {:from \"no-reply@bar.com\"
              :to \"someone@foo.com\"
              :subject \"Test mail\"
              :html \"Hi ,</br> How are you ?\"
              :attachment [(clojure.java.io/file \"path/to/file\")]}
             (fn [res] (println res)))"
  [{:keys [domain key] :as creds} message-content cb]
  (if (mail/validate-message message-content)
    (let [url     (mail/gen-url "/messages" domain)
          content (merge (mail/gen-auth key)
                         (mail/gen-body message-content)
                         {:keepalive 30000})]
      (http/post url content cb))
    (throw (Exception. "Invalid/Incomplete message-content"))))


(defn- mailgun-send!
  "Send an email asynchronously."
  [mailer to subject body {:keys [from cc uuid]
                           :or   {from senders/noreply}
                           :as   opts}]
  (let [out-c (chan 1)
        creds {:key    (:api-key mailer)
               :domain (:domain mailer)}
        data  {:from    (or (:sender mailer) from)
               :to      (or (:send-to mailer) to)
               :cc      (when (some? cc) cc)
               :subject subject
               :html    body}]
    (send-mail-async creds data
                     (fn [res]
                       (try
                         (put! out-c (update res :body json/parse-string true))
                         (catch Throwable t
                           (put! out-c t)))))
    out-c))


(s/fdef mailgun-send!
        :args (s/cat :mailer #(satisfies? Mailer %)
                     :to string?
                     :subject string?
                     :body string?
                     :opts (s/keys :opt-un [::from ::uuid]))
        :ret ta/chan?)


;; =============================================================================
;; API
;; =============================================================================


(defprotocol Mailer
  "Interface to send email."
  (send
    [this to subject body]
    [this to subject body opts]
    "Send an email to `to` with `subject` and `body`."))


(defrecord Mailgun [api-key domain sender send-to])


(extend-protocol Mailer
  Mailgun
  (send
    ([this to subject body]
     (send this to subject body {}))
    ([this to subject body opts]
     (mailgun-send! this to subject body opts))))


(defn mailgun
  ([api-key domain]
   (mailgun api-key domain {}))
  ([api-key domain opts]
   (map->Mailgun {:api-key api-key
                  :domain  domain
                  :sender  (:sender opts)
                  :send-to (:send-to opts)})))
