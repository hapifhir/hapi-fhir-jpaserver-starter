# JPA Server Starter Smoke Tests

---

When updating the supporting HAPI-FHIR version, or making changes to the JPA server starter code itself, it is recommended to run basic smoke tests to ensure the basic functionality of the server is maintained. The goal of these tests is
to provide users a quick way to run groups of tests that correspond to various sections within the 
[HAPI-FHIR documentation][Link-HAPI-FHIR-docs].

### Requirements
Tests are written in IntelliJ [HTTP Client Request files][Link-HTTP-Client-Req-Intro]. Ultimate edition of IntelliJ
is required to run these tests.

For more details on integrated tooling and request syntax, there is [documentation available][Link-HTTP-Client-Req-Exploring]
on the jetbrains website, in addition to the [API reference][Link-HTTP-Client-Req-API].

### Formatting
Each test file corresponds to a given section within the hapifhir documentation as close as possible. For 
example, there is a `plain_server.rest` file, which attemps to smoke test all basic functionality outlined in the section
[within the docs][Link-HAPI-FHIR-docs-plain-server].

Individual tests are formatted as follows:
```javascript
### Test Title Here
# <link to relevant documentation>
REST-OPERATION ENDPOINT-URL
// Verification Steps
```

To run these tests against a specific server, configure the server details within the `http-client.env.json` file. By default, we provide the following:
```json
{
  "default": {
    "host": "localhost:8080",
    "username": "username",
    "password": "password"
  }
}
```

### Running the Tests
Within IntelliJ, right click the file you wish to run tests in and select the `Run All` option from the menu.

**Important:** Tests may not work individually when run. Often times, tests need to be run sequentially, as they depend
on resources/references from previous tests to complete. _(An example of this would be adding a Patient, saving the id, 
then using that saved id to test if we can successfully PATCH that Patient resource. Without that saved id from the 
previous test creating that patient, the PATCH test will fail.)_


[Link-HAPI-FHIR-docs]: https://hapifhir.io/hapi-fhir/docs/
[Link-HAPI-FHIR-docs-plain-server]: https://hapifhir.io/hapi-fhir/docs/server_plain/server_types.html
[Link-HTTP-Client-Req-Intro]: https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html
[Link-HTTP-Client-Req-Exploring]: https://www.jetbrains.com/help/idea/exploring-http-syntax.html
[Link-HTTP-Client-Req-API]: https://www.jetbrains.com/help/idea/http-response-handling-api-reference.html