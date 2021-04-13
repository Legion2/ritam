# Demo of RITAM

This demo shows how RITAM manages IoT devices and applications.
In this demo the device IoT environment is simulated by using multiple docker containers connected via a network.

## Prerequisite

* Docker
* Docker-compose

## Setup

1. Install the [`ritam` CLI](../cli).
2. Clone this repository and open a terminal in the `demo/` directory of the project.
3. Start all components by running `docker-compose up -d`. This will start the containers in the background, you can stop them with `docker-compose stop`.

## Demo

The demo consists of the `manager` container, which is the RITAM Device and Application Manager, it can be accessed at http://localhost:8080/swagger/ or via the `ritam` CLI.
Five IoT devices are simulated by five docker containers called `deviceX`, they can be accessed on localhost with the ports `8081-8085`.
For the demo, a MQTT broker is started with docker-compose, which is used in the demo IoT application.
To observe the MQTT messages run `docker-compose exec broker mosquitto_sub -t '#'`.

In the following the management operations of an example IoT application are demonstrated.
The IoT application reads temperature values from different IoT devices and sends them to the MQTT broker.
### Deploy the temperature reader on the devices

[`resources/temperature-app.yaml`](resources/temperature-app.yaml) defines an Application Template CRC Model for the temperature reader IoT application.
The application templates references the [`resources/temperature-sensor-service-template.yaml`](resources/temperature-sensor-service-template.yaml) TOSCA Service Template and selects devices with the `type: raspberry-pi` label.
It is deployed by applying the IaC definition file with the command `ritam apply application-templates resources/temperature-app.yaml`.

With `ritam get application-templates temperature-app --watch` the rollout of the new application can be observed.

The Application Template CRC Model of the temperature app contains a configuration error.
The MQTT broker url input parameter of the service template is not set, which is shown in the devices status.

```yaml
status:
  message: Successfully reconciled
  lastReconciled: 2021-04-12T16:12:57.218276Z
  instances: 3
  devices:
    device1: "up-to-date - Could not create Instance Model of: file:///resources/temperature-sensor-service-template.yaml"
    device2: "up-to-date - Could not create Instance Model of: file:///resources/temperature-sensor-service-template.yaml"
    device4: "up-to-date - Could not create Instance Model of: file:///resources/temperature-sensor-service-template.yaml"
```
### Update configuration on all devices

To fix the configuration the missing MQTT broker url was added in [`resources/temperature-app-patch.yaml`](resources/temperature-app-patch.yaml).
To rollout this update to all devices it must be applied with `ritam apply application-templates resources/temperature-app-patch.yaml`.

With `ritam get application-templates temperature-app --watch` the rollout of the patched configuration can be observed.
The status of the `temperature-app` application template shows on which devices it is deployed and what the status of the application is on the different devices.

```yaml
status:
  message: Successfully reconciled
  lastReconciled: 2021-04-12T16:22:15.472417Z
  instances: 3
  devices:
    device1: up-to-date - Successfully Reconciled
    device2: up-to-date - Successfully Reconciled
    device4: up-to-date - Successfully Reconciled
```

After the fixed configuration was deploy the temperature reader can successfully send temperature values to the MQTT broker, which can be seen with `docker-compose exec broker mosquitto_sub -t '#'`.

### Decommission the IoT application and delete it from the all devices

To decommission the application, it must be deleted from the RITAM Device and Application Manager.
The deletion is an asynchronous process.
To wait for its completion the `--wait` option is used:

`ritam delete application-templates temperature-app --wait`

When the command returns, the temperature IoT application was deleted from all devices and the RITAM Device and Application Manager.

## Cleanup after the demo

To cleanup all resources created by the demo run the following command:
`docker-compose down -v`
