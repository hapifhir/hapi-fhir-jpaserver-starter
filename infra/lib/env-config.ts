import { EnvType } from "./env-type";

export type EnvConfig = {
  environmentType: EnvType;
  region: string;
  vpcId: string;
  dbName: string;
  dbUsername: string;
  fhirServerECRName: string;
};
