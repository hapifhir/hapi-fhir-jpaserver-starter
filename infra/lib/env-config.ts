import { EnvType } from "./env-type";

export type EnvConfig = {
  environmentType: EnvType;
  region: string;
  vpcId: string;
  zone: string;
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
