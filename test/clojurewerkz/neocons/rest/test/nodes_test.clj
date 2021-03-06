;; Copyright (c) 2011-2018 Michael S. Klishin, Alex Petrov, and The ClojureWerkz
;; Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.neocons.rest.test.nodes-test
  (:require [clojurewerkz.neocons.rest       :as neorest]
            [clojurewerkz.neocons.rest.nodes :as nodes]
            [clojure.test                    :refer :all]
            [clojurewerkz.neocons.rest.records :refer [instantiate-node-from]]))

(let [conn (neorest/connect "http://localhost:7474/db/data/")]
  (deftest test-creating-and-immediately-accessing-a-node-without-properties
    (let [created-node (nodes/create conn)
          fetched-node (nodes/get conn (:id created-node))]
      (is (= (:id created-node) (:id fetched-node)))))

  (deftest test-creating-and-immediately-accessing-a-node-with-properties
    (let [data         {:key "value"}
          created-node (nodes/create conn data)
          fetched-node (nodes/get conn (:id created-node))]
      (is (= (:id created-node) (:id fetched-node)))
      (is (= (:data created-node) data))))


  (deftest test-creating-and-immediately-accessing-a-unique-node-in-an-index
    (let [data         {:name "Tobias" :value "test"}
          created-n1   (nodes/create-unique-in-index conn "vertices" "name" "Tobias" data)
          created-n2   (nodes/create-unique-in-index conn "vertices" "name" "Tobias" {})]
      (is (= (:id created-n1) (:id created-n2)))
      (is (= (:data created-n1) (:data created-n2)))
      (is (= (:data created-n1) data))
      (is (nodes/find-one conn "vertices" "name" "Tobias"))
      (is (not (nodes/find-one conn "vertices" "name" "asd09asud987987")))))


  (deftest test-accessing-a-non-existent-node
    (is (thrown? Exception
                 (nodes/get conn 928398827))))


  (deftest test-creating-and-deleting-a-node-with-properties
    (let [data         {:key "value"}
          created-node (nodes/create conn data)
          [deleted-id status] (nodes/delete conn (:id created-node))]
      (is (= 204 status))
      (is (= (:id created-node) deleted-id))))

  (deftest test-attempting-to-delete-a-non-existent-node
    (is (thrown? Exception
                 (nodes/delete conn 237737737))))


  (deftest test-creating-and-getting-properties-of-one-node
    (let [data         {:key "value"}
          created-node (nodes/create conn data)
          fetched-data (nodes/get-properties conn (:id created-node))]
      (is (= data fetched-data))))

  (deftest test-creating-and-getting-empty-properties-of-one-node
    (let [created-node (nodes/create conn)
          fetched-data (nodes/get-properties conn (:id created-node))]
      (is (= {} fetched-data))))


  (deftest test-updating-a-single-node-property
    (let [node         (nodes/create conn {:age 26})
          fetched-node (nodes/get conn (:id node))
          new-value    (nodes/set-property conn (:id node) :age 27)
          updated-node (nodes/get conn (:id fetched-node))]
      (is (= new-value (-> updated-node :data :age)))))


  (deftest test-updating-node-properties
    (let [node         (nodes/create conn {:age 26})
          fetched-node (nodes/get conn (:id node))
          new-data    (nodes/update conn (:id node) {:age 27 :gender "male"})
          updated-node (nodes/get conn (:id fetched-node))]
      (is (= new-data (-> updated-node :data)))))


  (deftest test-deleting-all-properties-from-a-node
    (let [data         {:key "value"}
          created-node (nodes/create conn data)
          fetched-data (nodes/get-properties conn (:id created-node))]
      (is (= data fetched-data))
      (nodes/delete-properties conn (:id created-node))
      (is (= {} (nodes/get-properties conn (:id created-node)))))))
