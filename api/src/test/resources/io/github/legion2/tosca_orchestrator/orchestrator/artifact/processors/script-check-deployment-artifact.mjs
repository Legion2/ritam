#!/usr/bin/env node
import { writeFileSync, existsSync } from "fs";

const daPath = process.env.DA_Path;

writeFileSync(process.env.TOSCA_OUTPUT_da_exists, JSON.stringify(existsSync(daPath)));
