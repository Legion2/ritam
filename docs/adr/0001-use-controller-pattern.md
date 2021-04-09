# Use Controller Pattern

* Status: accepted
* Deciders: Leon Kiefer
* Date: 2020-10-21

## Context and Problem Statement

The orchestration of TOSCA Service Template requires the invocation of multiple operations of the node templates in the right order and with the right parameters.
This process workflow can either be given imperatively or declaratively.
Imperative Workflows can directly be executed to orchestrate a Service Instance.
However, declarative Workflows are defined based on the Topology Template, which included the Node Templates, Relation Templates, Requirements and Capabilities, and can not be directly executed.
A declarative Workflow can be executed in two ways, (1) generate a Imperative Workflow based on the Topology Template and execute it or (2) immediately execute operations based on the Topology Template.
Both Have their Pros and Cons.

However, imperative and declarative Workflows are not resilient by default.
They rely on predefined state transitions caused be the defined operations.
Errors in these operations or unexpected state transitions often are not handled or lead to compensation of the complete workflow that in turn means the Service Instance will not be in the desired state.
As a result, Workflows are not suitable for unreliable environments and autonomous systems which must reach the desired state without human intervention.

## Decision Drivers

* eventually consistent system design
* unreliable environment
* autonomous systems

## Considered Options

* Workflows
* Controller and Reconciler Pattern

## Decision Outcome

Chosen option: "Controller and Reconciler Pattern", because it is resilient, self-healing and scales well.
