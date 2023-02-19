import { EnvConfig } from "../lib/env-config";
import { EnvType } from "../lib/env-type";

export const config: EnvConfig = {
  environmentType: EnvType.production,
  region: "us-east-1",
  vpcId: "my_vpcId",
  dbName: "my_db",
  dbUsername: "my_db_user",
  fhirServerECRName: "fhir-server-ecr-name",
};
export default config;
