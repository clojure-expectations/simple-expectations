(ns simple-expectations.core-test
  (:use expectations)
  (:require [simple-check.core :as sc]
            [simple-check.generators :as gen]
            [simple-check.properties :as prop]
            [simple-expectations.core :refer :all]))

;; the simple example from https://github.com/reiddraper/simple-check

(def prop-no-42
  (prop/for-all [v (gen/vector gen/int)]
    (not (some #{42} v))))

(sc/quick-check 100 prop-no-42)

;; simple integration with expectations

(expect {:result true} (in (sc/quick-check 100 prop-no-42)))

;; better integration with expectations

(defrecord SimpleCheck []
  CustomPred
  (expect-fn [e a] (:result a))
  (expected-message [e a str-e str-a] (format "%s of %s failures"
                                              (:failing-size a)
                                              (:num-tests a)))
  (actual-message [e a str-e str-a] (format "fail: %s" (:fail a)))
  (message [e a str-e str-a] (format "shrunk: %s" (get-in a [:shrunk :smallest]))))

(expect (->SimpleCheck) (sc/quick-check 100 prop-no-42))
