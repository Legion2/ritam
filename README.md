# Reconciliation based IoT Application Management (RITAM)
The code of the prototype created for my master's thesis

Architectural Decision Records can be found in [`docs/adr`](docs/adr/index)

This project uses Quarkus, the Supersonic Subatomic Java Framework.

The RITAM Device and Application Manger and the RITAM Orchestrator are implemented as one project, so the same executable is used for both components.
The implementation of the level-based API, storage, and controllers with the reconcilers can be found in [`api/`](api).
The TOSCA data model and parser is defined in the [`core/`](core) directory.
The source of the `ritam` CLI is in the [`cli/`](cli) directory.
A demo of RITAM is provided in [`demo/`](demo).

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
There is also a bash completion generated.

## Reconciler input/output

The Bash and Javascript Reconciler are called with the inputs given as environment variables.
The name of the environment variables is the same as the name of the input parameter.

The output is handled via text files, where the value must be written to.
The name of the output files is stored in environment variables.
Env: `$TOSCA_OUTPUT_<output>` where `<output>` is the name of the output parameter.
To provide an output value in a bash script the following command can be used:
`echo "foo" > $TOSCA_OUTPUT_bar`
or in Javascript:
```js
import { writeFileSync } from "fs";
writeFileSync(process.env.TOSCA_OUTPUT_bar, "foo");
```

## Delete Reconciler function

The delete reconciler function is called when the component should be deleted and cleaned up.
It has special output parameter named `deleted`, which should be set to `true` or `false` by the delete reconciler function to indicate if the cleanup process is completed.
