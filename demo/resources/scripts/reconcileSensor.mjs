import pm2 from "pm2";
import childProcess from "child_process";
import { promisify } from "util";
import { writeFileSync } from "fs";
import { dirname } from "path";
const exec = promisify(childProcess.exec);

const connectAsync = promisify(pm2.connect.bind(pm2));
const startAsync = promisify(pm2.start.bind(pm2));
const deleteAsync = promisify(pm2.delete.bind(pm2));
const describeAsync = promisify(pm2.describe.bind(pm2));

function getInput(parameter) {
  return JSON.parse(process.env[parameter]);
}

const indexFile = getInput("DA_INDEX");
const cwd = dirname(indexFile);

const { stdout, stderr } = await exec("npm ci", {
  cwd: cwd,
});
if (stderr) {
  console.error(`error: ${stderr}`);
}
console.log(stdout);

await connectAsync(true);

const name = "temperature-sensor";

const config = {
  name: name,
  script: indexFile,
  cwd: cwd,
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
