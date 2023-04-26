import { EnvType } from "./env-type";

export type EnvConfig = {
  environmentType: EnvType;
  region: string;
  vpcId: string;
  zoneId: string; // Route53 ID of the private hosted zone for this region
  domain: string;
  subdomain: string;
  dbName: string;
  dbUsername: string;
  slack?: {
    workspaceId: string;
    alertsChannelId: string;
    snsTopicArn: string;
  };
};
