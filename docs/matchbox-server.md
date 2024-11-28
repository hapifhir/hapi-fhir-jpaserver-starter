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
