# Metriport FHIR Server Infrastructure

This package is used to build and update the infrastructure required for Metriport's FHIR Server.

It uses AWS CDK + TypeScript.

## Deploy to AWS

It requires AWS CLI setup properly.

Run these commands on the terminal from the `./infra` folder of this repository:

```shell
$ cdk bootstrap -c env=<env> # only needs to be run once
$ ./deploy.sh
```

### Updating the configuration

Currently, the configuration is Base64 encoded and stored on GH secrets.

```shell
$ base64 -i infra/config/staging.ts
$ base64 -i infra/config/production.ts
$ base64 -i infra/config/sandbox.ts
```

Copy the resulting strings and update the respective secrets:
- `INFRA_CONFIG_STAGING`
- `INFRA_CONFIG_PRODUCTION`
- `INFRA_CONFIG_SANDBOX`