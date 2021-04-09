# Strict separation of spec and state

* Status: accepted
* Deciders: Leon Kiefer
* Date: 2020-11-11


## Context and Problem Statement

When deploying TOSCA services with all nodes and lifecycle states, there is an desired and actual state of each node.
The goal is to bring the actual state close to the desired state of the nodes.

It is important for this to strictly separate the two states.
So it is unambiguous what is only the expected state and what is the actual state of the system.
This is even more important when using the Controller and Reconciler Pattern.

So to better differentiate them we call them "spec" and "status".
The spec is the desired state of a resource it does not change based on the actual state of the resources.
Changes to the spec mean changes to the desired state.
The status is the actual state observed by the controller of the resource.
It can always be reconstructed by the controller if the value was lost.

The controller of a resource have to interact with the state, they read the spec and write the status of the resource.
A controller can therefore create other resources and read their status.

## Decision Drivers

* Decoupling of event chains
* Abstraction of internal state handling


## Decision Outcome

Chosen option: "Spec and Status are stored separated", because else it is not possible to process and interact with them separately.
