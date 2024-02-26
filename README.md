Gravitate-Health IPS FHIR Server
=================================================

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Table of contents
-----------------

- [Gravitate-Health IPS FHIR Server](#gravitate-health-ips-fhir-server)
  - [Table of contents](#table-of-contents)
  - [Introduction](#introduction)
  - [Installation](#installation)
    - [FHIR deployment](#fhir-deployment)
  - [Usage](#usage)
  - [Known issues and limitations](#known-issues-and-limitations)
  - [Getting help](#getting-help)
  - [Contributing](#contributing)
  - [License](#license)
  - [Authors and history](#authors-and-history)
  - [Acknowledgments](#acknowledgments)


Introduction
------------
This repository contains the files needed for the deployment of a IPS FHIR Server in a Kubernetes cluster. For the instalation the public [Helm Chart](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/tree/master/charts/hapi-fhir-jpaserver) is used, as well as the official version of [HAPI FHIR JPA Server Starter](https://github.com/hapifhir/hapi-fhir-jpaserver-starter).

Installation
------------

### FHIR deployment

Refer to the [General FOSPS Deployment Documentation](https://github.com/Gravitate-Health/Documentation) to deploy this service.


Usage
-----

Please refer to the official documentation of [HAPI FHIR](https://hapifhir.io/hapi-fhir/docs/).


Known issues and limitations
----------------------------
None are known at this time

Getting help
------------

In case you find a problem or you need extra help, please use the issues tab to report the issue.

Contributing
------------

To contribute, fork this repository and send a pull request with the changes squashed.

License
-------
This project is distributed under the terms of the [Apache License, Version 2.0 (AL2)](http://www.apache.org/licenses/LICENSE-2.0).  The license applies to this file and other files in the [GitHub repository](https://github.com/Gravitate-Health/Gateway) hosting this file.

```
Copyright 2022 Universidad Politécnica de Madrid

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

Authors and history
---------------------------
- Jens Kristian Villadsen ([@jkiddo](https://github.com/jkiddo))
- Álvaro Belmar ([@abelmarm](https://github.com/abelmarm))

Acknowledgments
---------------
 - [HAPI FHIR Server project](https://github.com/hapifhir/hapi-fhir)

