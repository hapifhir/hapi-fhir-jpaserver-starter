#!/usr/bin/env node
import * as cdk from "aws-cdk-lib";
import "source-map-support/register";
import { BaseStack } from "../lib/base-stack";
import { EnvConfig } from "../lib/env-config";
import { EnvType } from "../lib/env-type";
import { FHIRServerStack } from "../lib/fhir-server-stack";

const app = new cdk.App();
//-------------------------------------------
// Parse config based on specified env
//-------------------------------------------
async function getConfig(): Promise<EnvConfig> {
  const env = app.node.tryGetContext("env");
  const validVals = Object.values(EnvType);
  if (!env || !validVals.includes(env)) {
    throw new Error(
      `Context variable missing on CDK command. Pass in as "-c env=XXX". Valid values are: ${validVals}`
    );
  }
  const configPath = `../config/${env}.ts`;
  const config = await import(configPath);
  if (!config || !config.default) {
    throw new Error(
      `Ensure config is defined, could not fine file ${configPath}`
    );
  }
  return config.default;
}

//-------------------------------------------
// Deploy the corresponding stacks
//-------------------------------------------
async function deploy() {
  const config = await getConfig();

  // CDK_DEFAULT_ACCOUNT will come from your AWS CLI account profile you've setup.
  // To specify a different profile, you can use the profile flag. For example:
  //    cdk synth --profile prod-profile
  const env = {
    account: process.env.CDK_DEFAULT_ACCOUNT,
    region: config.region,
  };

  //---------------------------------------------------------------------------------
  // Deploy the base stack - it should be done prior to the other ones.
  //---------------------------------------------------------------------------------
  new BaseStack(app, "BaseStack", { env, config });

  //---------------------------------------------------------------------------------
  // Deploy the FHIR server stack.
  //---------------------------------------------------------------------------------
  new FHIRServerStack(app, "FHIRServerStack", { env, config });

  //---------------------------------------------------------------------------------
  // Execute the updates on AWS
  //---------------------------------------------------------------------------------
  app.synth();
}

deploy();
