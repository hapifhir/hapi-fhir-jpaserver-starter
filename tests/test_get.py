import pytest
import random
import requests

URL = "http://localhost:8080/hapi-fhir-jpaserver/fhir/"


def test_GET_organization_random_unknown_name_fail():
    non_existent_name = f"SHOULD-NOT-EXIST-{random.random()}"
    # URL similar to "http://localhost:8080/hapi-fhir-jpaserver/fhir/Organization?name=SHOULD-NOT-EXIST-0.9383671425079436"
    response = requests.get(URL + "Organization", params={"name": non_existent_name})
    assert response.status_code == 200, response.json()
