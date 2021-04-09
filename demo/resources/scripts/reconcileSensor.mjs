import pm2 from "pm2";
import { promisify } from "util";
import { writeFileSync } from "fs";

const connectAsync = promisify(pm2.connect.bind(pm2));
const startAsync = promisify(pm2.start.bind(pm2));
const deleteAsync = promisify(pm2.delete.bind(pm2));
const describeAsync = promisify(pm2.describe.bind(pm2));

await connectAsync(true);

const name = "temperature-sensor";

const config = {
  name: name,
  script: "/resources/sensor/index.mjs",
  cwd: "/resources/sensor/",
};

let status = "unknown";

try {
  const desc = await describeAsync(name);
  await deleteAsync(name);
  await startAsync(config);
  status = `updated - ${desc[0].pm2_env.status}`;
} catch (error) {
  await startAsync(config);
  status = "started";
}

writeFileSync(process.env.TOSCA_OUTPUT_status, JSON.stringify(status));
pm2.disconnect();
