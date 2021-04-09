#!/usr/bin/env node
import MQTT from "async-mqtt";
import parse from "parse-duration";

function getInput(parameter) {
  return JSON.parse(process.env[parameter]);
}

const mqttUrl = getInput("MQTT_URL");
const location = getInput("location");
const interval = parse(getInput("interval"));
const baseTopic = "temperature/";

const client = await MQTT.connectAsync(mqttUrl, {
  clientId: `sensor-${location}`,
  connectTimeout: 2000,
  reconnectPeriod: 5000,
});
console.log("Connected");
setInterval(async () => {
  const temp = 15 + Math.random() * 10;
  await client.publish(
    baseTopic + location,
    JSON.stringify({
      timestamp: new Date(),
      temperature: temp,
      location: location,
    })
  );
}, interval);
