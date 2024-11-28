# Matchbox Server


## Installing an NPM package through the operation $install-npm-package

The operation `$install-npm-package` allows you to install an NPM package through the API.
It is available if and only if `httpReadOnly` is disabled.

| Parameter IN | Card | Description                                 |
|--------------|------|---------------------------------------------|
| name         | 1..1 | The name of the package.                    |
| version      | 1..1 | The version of the package.                 |
| body         | 1..1 | The content of the NPM package as HTTP body |

```http
POST /matchboxv3/fhir/$install-npm-package?name=ch.fhir.ig.ch-core&version=4.0.1
Content-Type: application/gzip

<gzip content of the NPM package>
```

## Auto-installation of FHIR packages in the $validate operation

The operation `$validate` will try to install the FHIR package if it is not already installed, if and only if
`httpReadOnly` is disabled.

The FHIR package to install will be determined by (in the implemented order):

1. The `ig` parameter, if present;
2. The `profile` parameter. Matchbox will search for IGs that contain that canonical with the
   [Simplifier API](https://app.swaggerhub.com/apis-docs/firely/Simplifier.net_FHIR_Package_API/1.0.1) and install 
   the first result.
