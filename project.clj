(defproject starcity/mailer "0.6.0-SNAPSHOT"
  :description "Mail service for Starcity."
  :url "https://github.com/starcity-properties/mailer"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.3.443"]
                 [starcity/toolbelt-async "0.4.0"]
                 [nilenso/mailgun "0.2.3"]
                 [http-kit "2.2.0"]
                 [hiccup "1.0.5"]
                 [cheshire "5.7.1"]]

  :plugins [[s3-wagon-private "1.2.0"]]

  :repositories {"releases" {:url        "s3://starjars/releases"
                             :username   :env/aws_access_key
                             :passphrase :env/aws_secret_key}})
