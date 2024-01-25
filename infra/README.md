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
