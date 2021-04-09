# Explicit Node definition

* Status: accepted
* Deciders: Leon Kiefer
* Date: 2020-11-05

## Context and Problem Statement

The tosca specification allows topology templates to be incomplete.
Which means not all nodes and relationships have to be explicitly defined by using NodeTemplates and RelationshipTemplates in the topology template.
Missing nodes and relationships should be generated based on requirements and capabilities of the existing nodes and a repository of the orchestrator.

This is useful for creating abstract and reusable topology templates that can be completed by different orchestrator implementations based on their environment.
For example, the compute nodes are not explicitly defined, so orchestrators can choose from their own available resources based on requirements and capabilities.

In den context of on-device orchestration for edge or IoT devices, two orchestrators have to be differentiated.
One orchestrator runs on the devices and is only responsible to orchestrate applications on that devices and the other orchestrator manages applications across multiple Edge and IoT devices and the cloud.
So, while we want to keep the topology templates abstract and reusable, there is a stronger coupling between the devices capabilities and the requirements of the application.
For example, a Presence detection IoT application uses Bluetooth to detect nearby devices.
This IoT application not only requires a Bluetooth interface but also requires a specific physical location to detect nearby devices.
Therefore, this application can not be orchestrated on some device with bluetooth interface, but must be orchestrated on a specific devices at a specific location in a building.

The selection of the devices to host IoT and Edge application on is an important part of the orchestration process, but not handled by the on device orchestrator.
Therefore the TOSCA Service Templates which are deployed on the on device orchestrator can not select form a resource pool.

## Decision Drivers
* no resource pool
* location factor

## Considered Options

* Explicit Node definition

## Decision Outcome

Chosen option: "Explicit Node definition", because the on device orchestrator is much simpler and topology templates do not rely on implicit features of the orchestrator to complete the topology.
