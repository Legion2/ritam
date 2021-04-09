#!/usr/bin/env node
import { writeFileSync } from "fs";

const input = process.env.input;

writeFileSync(process.env.TOSCA_OUTPUT_status, "example status");
writeFileSync(process.env.TOSCA_OUTPUT_input, `input: ${input}`);
