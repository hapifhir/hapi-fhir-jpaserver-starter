import { CfnOutput, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import * as ecr from "aws-cdk-lib/aws-ecr";
import { Construct } from "constructs";
import { EnvConfig } from "./env-config";

interface BaseStackProps extends StackProps {
  config: EnvConfig;
}

/**
 * This is a base stack used used to create assets that should be deployed
 * before the rest of the infrastructure.
 */
export class BaseStack extends Stack {
  constructor(scope: Construct, id: string, props: BaseStackProps) {
    super(scope, id, props);

    const fhirECR = this.setupFHIRECR(props);

    //-------------------------------------------
    // Output
    //-------------------------------------------
    new CfnOutput(this, `FHIR Server ECR ARN`, {
      value: fhirECR.repositoryArn,
    });
  }

  private setupFHIRECR(props: BaseStackProps): ecr.IRepository {
    return new ecr.Repository(this, props.config.fhirServerECRName, {
      repositoryName: props.config.fhirServerECRName,
      removalPolicy: RemovalPolicy.DESTROY,
    });
  }
}
