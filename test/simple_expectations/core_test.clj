(ns simple-expectations.core-test
  (:use expectations)
  (:require [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [expectations :refer :all]))

;; the simple example from https://github.com/reiddraper/simple-check

(def prop-no-42
    (prop/for-all [v (gen/vector gen/int)]
                      (not (some #{42} v))))

(tc/quick-check 100 prop-no-42)

;; simple integration with expectations

(expect {:result true} (in (tc/quick-check 100 prop-no-42)))

;; better integration with expectations

(defrecord SimpleCheck []
    CustomPred
    (expect-fn [e a] (true? (:result a)))
    (expected-message [e a str-e str-a] (format "%s of %s failures"
                                                                                              (:failing-size a)
                                                                                              (:num-tests a)))
    (actual-message [e a str-e str-a] (format "fail: %s" (:fail a)))
    (message [e a str-e str-a] (format "shrunk: %s" (get-in a [:shrunk :smallest]))))

(expect (->SimpleCheck) (tc/quick-check 100 prop-no-42))


;; Handling so unexpected exceptions do not return as passed

(def prop-that-throws-exception
  (prop/for-all [v gen/int]
                (throw (ex-info "Exception" {}))))

(expect (->SimpleCheck) (tc/quick-check 100 prop-that-throws-exception))
