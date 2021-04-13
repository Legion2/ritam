import pm2 from "pm2";
import { promisify } from "util";
import { promises as fs } from "fs";
import { dirname } from "path";

const connectAsync = promisify(pm2.connect.bind(pm2));
const deleteAsync = promisify(pm2.delete.bind(pm2));
const describeAsync = promisify(pm2.describe.bind(pm2));

function getInput(parameter) {
  return JSON.parse(process.env[parameter]);
}

const indexFile = getInput("DA_INDEX");
const cwd = dirname(indexFile);

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
  await fs.rm(cwd, { recursive: true });
  deleted = true;
  status = "deleted";
}

await fs.writeFile(process.env.TOSCA_OUTPUT_status, JSON.stringify(status));
await fs.writeFile(process.env.TOSCA_OUTPUT_deleted, JSON.stringify(deleted));
pm2.disconnect();
