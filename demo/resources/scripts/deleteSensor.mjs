import pm2 from "pm2";
import { promisify } from "util";
import { writeFileSync } from "fs";

const connectAsync = promisify(pm2.connect.bind(pm2));
const deleteAsync = promisify(pm2.delete.bind(pm2));
const describeAsync = promisify(pm2.describe.bind(pm2));

await connectAsync(true);

const name = "temperature-sensor";
let status = "unknown";
try {
  const proc = await deleteAsync(name);
  status = proc.status;
} catch (error) {
  //status = error
}

let deleted = false;
try {
  const desc = await describeAsync(name);
  status = desc[0].pm2_env.status;
  deleted = false;
} catch (error) {
  deleted = true;
  status = "deleted";
}

writeFileSync(process.env.TOSCA_OUTPUT_status, JSON.stringify(status));
writeFileSync(process.env.TOSCA_OUTPUT_deleted, JSON.stringify(deleted));
pm2.disconnect();
