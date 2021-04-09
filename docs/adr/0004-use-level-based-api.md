# Use Level Based API

* Status: accepted
* Deciders: Leon Kiefer
* Date: 2020-11-12


## Context and Problem Statement

How to manage desired state in a distributed system, where two parties, the operators and the controllers interact with the same data objects.
How to observe changes in the spec (expected state).

## Decision Drivers <!-- optional -->

* Must work well with Controller and Reconciler Pattern
* must be idempotent

## Considered Options

* Level Based API
* Other API

## Decision Outcome

Chosen option: "Level Based API", because it allows to define the desired state using Infrastructure as Code and decouples operators and controllers.

