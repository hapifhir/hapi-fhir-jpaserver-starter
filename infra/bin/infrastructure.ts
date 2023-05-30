#!/usr/bin/env node
import * as cdk from "aws-cdk-lib";
import "source-map-support/register";
import { EnvConfig } from "../lib/env-config";
import { FHIRServerStack } from "../lib/fhir-server-stack";
import { initConfig } from "../lib/shared/config";

const app = new cdk.App();

//-------------------------------------------
// Deploy the corresponding stacks
//-------------------------------------------
async function deploy(config: EnvConfig) {
  // CDK_DEFAULT_ACCOUNT will come from your AWS CLI account profile you've setup.
  // To specify a different profile, you can use the profile flag. For example:
  //    cdk synth --profile prod-profile
  const env = {
    account: process.env.CDK_DEFAULT_ACCOUNT,
    region: config.region,
  };

  //---------------------------------------------------------------------------------
  // Deploy the FHIR server stack.
  //---------------------------------------------------------------------------------
  new FHIRServerStack(app, "FHIRServerStack", { env, config });

  //---------------------------------------------------------------------------------
  // Execute the updates on AWS
  //---------------------------------------------------------------------------------
  app.synth();
}

initConfig(app.node).then((config) => deploy(config));
