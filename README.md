# Reconciliation based IoT Application Management (RITAM)
This project is part of the master's thesis "Concept and Implementation of a TOSCA Orchestration Engine for Edge and IoT Infrastructures".
This repository contains the prototype of RITAM.

The RITAM Device and Application Manger and the RITAM Orchestrator are implemented in the same executable.
The implementation of the level-based API, storage, and controllers with the reconcilers can be found in the [`api`](api) module.
The TOSCA data model and parser is defined in the [`core`](core) module.
The source of the `ritam` CLI is hosted in the [`cli/`](cli) directory.
A demo of RITAM is provided in [`demo/`](demo).

This project is written in Kotlin and uses Quarkus.
Architectural Decision Records can be found in [`docs/adr`](docs/adr/index.md)

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw -pl api compile quarkus:dev
```

## Build the project

To build the RITAM Device and Application Manger and RITAM Orchestrator running `./mvnw package -DskipTests`.
This will create the `ghcr.io/legion2/ritam-api` docker image and the executable application in `api/target/quarkus-app`.
Note: Docker must be running or disabled with the following option `-Dquarkus.container-image.build=false`.

The CLI can be build by running `./gradlew runtimeZip` in the `cli/` directory.
This will generate the `cli/build/ritam-*.zip` files, which contain all executables to run the `ritam` CLI.
More infos about the CLI can be found in the [`cli/`](cli) directory.

## TOSCA Reconciler Interface Usage
RITAM introduces the TOSCA Reconciler Interface, for reconciliation based TOSCA Node operations.
It has the name `ritam.interfaces.Reconciler` and is part of the `https://legion2.github.io/ritam` namespace.
It can be imported from `https://legion2.github.io/ritam/ritam.yaml`.
The interface provides two operations: `reconcile` and `delete`.

### Reconciler input/output
The prototype supports Bash and Javascript implementation artifact types.
The inputs are given as environment variables.
The name of the environment variables is the same as the name of the input parameter.
Input and output variable values are json encoded.

The output is handled via text files, where the value must be written to.
The name of the output files is stored in environment variables.
Env: `$TOSCA_OUTPUT_<output>` where `<output>` is the name of the output parameter.
To provide an output value in a bash script the following command can be used:
`echo "foo" > $TOSCA_OUTPUT_bar`
or in Javascript:
```js
import { writeFileSync } from "fs";
writeFileSync(process.env.TOSCA_OUTPUT_bar, JSON.stringify("foo"));
```

### Delete Reconciler operation

The `delete` reconciler operation is called when the component should be deleted and cleaned up.
It has special output parameter named `deleted`, which should be set to `true` or `false` by the `delete` reconciler operation to indicate if the cleanup process is completed.
To be able to set the `deleted` output parameter, it must be mapped in the operation output definition.

```yaml
operations:
  delete:
    outputs:
      deleted: [SELF, deleted]
```
