(define (domain ambulance world)
    (:predicates (Location ?l) (Road ?a ?b) (Patient ?p) (Ambulance ?a) (Hospital ?h) (At ?o ?l) (Available ?a) (Waiting ?p) (InHospital ?p) (In ?p ?a))
    (:functions (LocationCoord ?l) (LocationDemand ?l) (Distance ?a ?b) (Priority ?p))
    (:action move
      :parameters (?a ?from ?to)
      :precondition (and (Ambulance ?a) (Location ?from) (Location ?to) (At ?from) (Road ?from ?to))
      :effect (and (At ?to) (not (At ?from))))
    (:action pick
      :parameters (?p ?a ?l)
      :precondition (and (Patient ?p) (Ambulance a) (Location ?l) (Waiting ?p) (Available ?a) (At ?p ?l) (At ?a ?l))
      :effect (and (not (Waiting ?p)) (not (Available ?a)) (In ?p ?a)))
    (:action drop
      :parameters (?p ?a ?h ?l)
      :precondition (and (Patient ? p) (Ambulance ?a) (Hospital ?h) (Location ?l) (At ?a ?l) (At ?h ?l) (In ?p ?a))
      :effect (and (not (In ?p ?a)) (InHospital ?p) (Available ?a))))