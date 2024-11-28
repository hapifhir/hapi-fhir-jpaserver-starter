import click
import json
import requests
import os
import yaml


@click.group()
def cli():
    pass


@cli.command()
@click.option(
    '--fhir-url', '-u',
    default="http://localhost:8080/fhir",
    help="Base URL of the FHIR service (e.g., 'https://fhir.example.com')."
)
def get_resource_counts(fhir_url):
    """
    Counts from `get-resource-counts`.
    """
    try:
        # Validate the FHIR base URL
        if not fhir_url.endswith('/'):
            fhir_url += '/'

        # get-resource-counts
        # Send GET request to FHIR service
        response = requests.get(f"{fhir_url}$get-resource-counts")
        if response.status_code == 200:
            metadata = response.json()
            yaml_metadata = yaml.dump(metadata, default_flow_style=False)
            print(yaml_metadata)
        else:
            click.echo(f"Failed with status {response.status_code}: {response.text}")

    except Exception as e:
        click.echo(f"Error: {e}")


@cli.command()
@click.option(
    '--fhir-url', '-u',
    default="http://localhost:8080/fhir",
    help="Base URL of the FHIR service (e.g., 'https://fhir.example.com')."
)
def count_resources(fhir_url):
    """
    Explicitly count every resource type.
    """
    try:
        # Validate the FHIR base URL
        if not fhir_url.endswith('/'):
            fhir_url += '/'

        # Send GET request to FHIR service
        response = requests.get(f"{fhir_url}metadata")
        if response.status_code == 200:
            metadata = response.json()
            resource_types = [_['type'] for _ in metadata['rest'][0]['resource']]
            for resource_type in resource_types:
                response = requests.get(f"{fhir_url}{resource_type}?_count=0&_total=accurate")
                if response.status_code == 200:
                    total = response.json().get('total')
                    if total:
                        click.echo(f"{resource_type}: {total}")
                else:
                    click.echo(f"Failed with status {response.status_code}: {response.text}")
        else:
            click.echo(f"Failed with status {response.status_code}: {response.text}")

    except Exception as e:
        click.echo(f"Error: {e}")


@cli.command()
@click.option(
    '--directory', '-d',
    type=click.Path(exists=True, file_okay=False),
    required=True,
    help="Path to the directory containing NDJSON files."
)
@click.option(
    '--fhir-url', '-u',
    default="http://localhost:8080/fhir",
    help="Base URL of the FHIR service (e.g., 'https://fhir.example.com')."
)
@click.option(
    '--auth-token', '-t',
    default=None,
    help="Optional authentication token for the FHIR service."
)
def push(directory, fhir_url, auth_token):
    """
    Send all NDJSON files in a directory to server. See `$import` for faster method.
    """
    try:
        # Validate the FHIR base URL
        if not fhir_url.endswith('/'):
            fhir_url += '/'

        # Prepare headers
        headers = {
            'Content-Type': 'application/json',
        }
        if auth_token:
            headers['Authorization'] = f"Bearer {auth_token}"

        # Process all NDJSON files in the directory
        for filename in os.listdir(directory):
            if filename.endswith('.ndjson'):
                filepath = os.path.join(directory, filename)
                click.echo(f"Processing file: {filepath}")

                with open(filepath, 'r') as ndjson_file:
                    for line_number, line in enumerate(ndjson_file, start=1):
                        try:
                            # Parse JSON
                            entry = json.loads(line.strip())
                            resource_type = entry.get('resourceType')

                            if not resource_type:
                                click.echo(f"Line {line_number}: Missing resourceType field, skipping.")
                                continue

                            # Construct FHIR endpoint
                            id_ = entry.get('id')
                            assert id_, f"Line {line_number}: Missing id field."
                            fhir_endpoint = f"{fhir_url}{resource_type}/{id_}"

                            # Send PUT request to FHIR service, since we have the id in the resource
                            response = requests.put(fhir_endpoint, headers=headers, json=entry)
                            if response.status_code in [200, 201]:
                                click.echo(f"Line {line_number}: Successfully sent to {resource_type}.")
                            else:
                                click.echo(f"Line {line_number}: Failed with status {response.status_code}: {response.text}")

                        except json.JSONDecodeError as e:
                            click.echo(f"Line {line_number}: Invalid JSON - {e}")

    except Exception as e:
        click.echo(f"Error: {e}")


def fetch_all_patient_revincludes(fhir_url, headers, bundle):
    """Retrieve all the resources that refer to Patients"""
    patient_ids = [_['resource']['id'] for _ in bundle['entry'] if _['resource']['resourceType'] == 'Patient']
    for patient_id in patient_ids:
        response = requests.get(f"{fhir_url}Patient/{patient_id}/$everything", headers=headers)
        if response.status_code == 200:
            patient_bundle = response.json()
            bundle['entry'].extend([_ for _ in patient_bundle['entry'] if _['resource']['resourceType'] not in ['Patient', 'ResearchSubject']])
        else:
            click.echo(f"Failed with status {response.status_code}: {response.text}")


def fetch_all_specimen_revincludes(fhir_url, headers, bundle):
    """Retrieve all the resources that refer to Specimens in the bundle"""
    specimen_ids = [_['resource']['id'] for _ in bundle['entry'] if _['resource']['resourceType'] == 'Specimen']
    for specimen_id in specimen_ids:
        response = requests.get(f"{fhir_url}Specimen/{specimen_id}/$everything", headers=headers)
        if response.status_code == 200:
            specimen_bundle = response.json()
            bundle['entry'].extend([_ for _ in specimen_bundle['entry'] if _['resource']['resourceType'] not in ['Patient', 'ResearchSubject']])
        else:
            click.echo(f"Failed with status {response.status_code}: {response.text}")


@cli.command()
@click.option(
    '--fhir-url', '-u',
    default="http://localhost:8080/fhir",
    help="Base URL of the FHIR service (e.g., 'https://fhir.example.com')."
)
@click.option(
    '--auth-token', '-t',
    default=None,
    help="Optional authentication token for the FHIR service."
)
@click.option(
    '--identifier', '-i',
    default=None,
    help="ResearchStudy identifier."
)
def fetch(fhir_url, auth_token, identifier):
    """
    Fetches all resources in a ResearchStudy from a FHIR service.
    """
    try:
        # Validate the FHIR base URL
        if not fhir_url.endswith('/'):
            fhir_url += '/'

        # Prepare headers
        headers = {
            'Content-Type': 'application/json',
        }
        if auth_token:
            headers['Authorization'] = f"Bearer {auth_token}"

        # Send GET request to fetch all ResearchStudy resources
        response = requests.get(f"{fhir_url}ResearchStudy?identifier={identifier}&_revinclude=ResearchSubject:study&_include=ResearchSubject:subject", headers=headers)
        if response.status_code == 200:
            # the bundle should have all ResearchStudy, ResearchSubject, and Patient resources
            bundle = response.json()

            # now fetch all the resources that refer to Patients
            fetch_all_patient_revincludes(fhir_url, headers, bundle)

            # now fetch all the resources that refer to Specimens
            fetch_all_specimen_revincludes(fhir_url, headers, bundle)

            # print the bundle
            click.echo(json.dumps(bundle, indent=2))
        else:
            click.echo(f"Failed with status {response.status_code}: {response.text}")

    except Exception as e:
        click.echo(f"Error: {e}")

if __name__ == '__main__':
    cli()
