#!/usr/bin/env node
import { writeFileSync } from "fs";
import parse from "parse-duration";

const duration = parse(process.env.duration);
writeFileSync(process.env.TOSCA_OUTPUT_duration, JSON.stringify(duration));
