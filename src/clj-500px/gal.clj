(ns clj-500px.gal
  (:require [clj-500px.oauth-ext :as oa]
            [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))

(def consumer-key "key")
(def consumer-secret "secret")
(def username "user")
(def password "pass")



(def base-url "https://api.500px.com/v1")

(defn build-url [part] (str base-url part))

(def req-token-url (build-url "/oauth/request_token"))
(def acc-token-url (build-url "/oauth/access_token"))
(def authorize-url (build-url "/oauth/authorize"))

(defn make-base
  [consumer-key consumer-secret username password]
  (let [consumer (oa/make-consumer consumer-key
                                   consumer-secret
                                   req-token-url
                                   acc-token-url
                                   authorize-url
                                   :hmac-sha1)
        request-token (oa/request-token consumer)
        {token :oauth_token token-secret :oauth_token_secret} (oa/xauth-access-token consumer request-token username password)]
    {:consumer consumer :token token :token-secret token-secret}))


(defn credentials
  [{:keys [consumer token token-secret]} url method & [data]]
  (oa/credentials consumer token token-secret method url data))

(defn auth-header [base url method params]
  (-> (credentials base url method params)
      (oa/authorization-header)))

(defn request [base method path params-key params]
  (let [full-url (build-url path)
        auth-header (auth-header base full-url method params)]
    (http/request {:method method :url full-url :headers {"Authorization" auth-header} params-key params})))

(def get #(request %1 :get %2 :query-params %3))
(def post #(request %1 :post %2 :form-params %3))

(defn parse-json [json-str]
  (json/read-str json-str :key-fn keyword))

(defn get-and-parse
  ([base path params]
   (-> (get base path params)
       (:body)
       (parse-json)))
  ([base path params resp-key]
   (-> (get base path params)
       (:body)
       (parse-json)
       (resp-key))))

(defn list-photos
  ([base] (list-photos base {}))
  ([base {:keys [feature rpp] :or {feature "fresh_today" rpp 20}}]
   (get-and-parse base "/photos.json" {:include_states 1 :feature feature :rpp rpp} :photos)))


(defn list-galleries
  ([base]
   (get-and-parse base "/collections" {} :collections)))


(defn list-gallery
  ([base id]
   (get-and-parse base (str "/collections/" id) {})))

(defn list-photo
  ([base id]
   (get-and-parse base (str "/photos/" id) {:image_size 7} :photo)))

(defn list-photo-url
  ([base id]
   (-> (list-photo base id)
       (:image_url))))

(defn like [base {id :id}]
  (post base (str "/photos/" id "/vote") {:vote 1}))

(defn comment-on [base {id :id} text]
  (post base (str "/photos/" id "/comments") {:body text}))

(let [fh-base (make-base consumer-key consumer-secret username password)]
  (println "base " fh-base)
  (->> (list-gallery fh-base "28445041")
       (:photos)
       (map #(% :id))
       (map (fn [id] (list-photo-url fh-base id)))))


(let [fh-base (make-base consumer-key consumer-secret username password)]
  (println "base " fh-base)
  (list-photo-url fh-base "219146699"))


(defn download [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(download "https://drscdn.500px.org/photo/221844675/h%3D1080/v2?client_application_id=32897&user_id=3077269&webp=true&sig=0c840457f49f94c53ad73755b36c666ecf69214964312938115f34dea2b3abc0" "/Users/danix/Prog/clojure/1.jpg")