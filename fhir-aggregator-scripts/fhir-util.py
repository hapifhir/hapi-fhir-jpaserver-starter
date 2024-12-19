import click
import json
import requests
import os
import yaml
from halo import Halo
import sys

FHIR_BASE = 'https://hapi.fhir-aggregator.org/fhir/'



@click.group()
def cli():
    pass


@cli.command()
@click.option(
    '--fhir-url', '-u',
    default=FHIR_BASE,
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
    default=FHIR_BASE,
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
        output = {'resources': {}}
        resources = output['resources']
        with Halo(text='Querying', spinner='line', placement='right', color='white', stream=sys.stderr) as spinner:
            spinner.text = f"Querying metadata"
            response = requests.get(f"{fhir_url}metadata")
            if response.status_code == 200:
                metadata = response.json()
                resource_types = [_['type'] for _ in metadata['rest'][0]['resource']]
                for resource_type in resource_types:
                    spinner.text = f"Querying {resource_type}"
                    response = requests.get(f"{fhir_url}{resource_type}?_count=0&_total=accurate")
                    if response.status_code == 200:
                        total = response.json().get('total')
                        if total:
                            resources[resource_type] = total
                    else:
                        click.echo(f"Failed with status {response.status_code}: {response.text}", file=sys.stderr)
                        break
            else:
                click.echo(f"Failed with status {response.status_code}: {response.text}", file=sys.stderr)
        # write output as yaml
        yaml_output = yaml.dump(output, default_flow_style=False)
        print(yaml_output)
    except Exception as e:
        click.echo(f"Error: {e}")


@cli.command()
@click.option(
    '--fhir-url', '-u',
    default=FHIR_BASE,
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
        output = {'resources': {}}
        resources = output['resources']
        with Halo(text='Querying', spinner='line', placement='right', color='white', stream=sys.stderr) as spinner:
            spinner.text = f"Querying metadata"
            response = requests.get(f"{fhir_url}metadata")
            if response.status_code == 200:
                metadata = response.json()
                resource_types = [_['type'] for _ in metadata['rest'][0]['resource']]
                for resource_type in resource_types:
                    spinner.text = f"Querying {resource_type}"
                    response = requests.get(f"{fhir_url}{resource_type}?_count=0&_total=accurate")
                    if response.status_code == 200:
                        total = response.json().get('total')
                        if total:
                            resources[resource_type] = total
                    else:
                        click.echo(f"Failed with status {response.status_code}: {response.text}", file=sys.stderr)
                        break
            else:
                click.echo(f"Failed with status {response.status_code}: {response.text}", file=sys.stderr)
        # write output as yaml
        yaml_output = yaml.dump(output, default_flow_style=False)
        print(yaml_output)
    except Exception as e:
        click.echo(f"Error: {e}")


@cli.command()
@click.option(
    '--fhir-url', '-u',
    default=FHIR_BASE,
    help="Base URL of the FHIR service (e.g., 'https://fhir.example.com')."
)
@click.argument('resource_type', required=True)
def summarize(fhir_url, resource_type):
    """
    List Summary information for resource

    \b
    RESOURCE_TYPE: The type of FHIR resource to summarize.
    """
    try:
        # Validate the FHIR base URL
        if not fhir_url.endswith('/'):
            fhir_url += '/'

        # Prepare headers
        headers = {
            'Content-Type': 'application/json',
        }

        entries = []
        with Halo(text='Querying', spinner='line', placement='right', color='white', stream=sys.stderr) as spinner:
            spinner.text = f"Querying {resource_type}"
            response = requests.get(f"{fhir_url}{resource_type}?_summary=summary", headers=headers)
            if response.status_code == 200:
                summary = response.json()
                entries = [_['resource'] for _ in summary['entry']]
            else:
                click.echo(f"Failed with status {response.status_code}: {response.text}", file=sys.stderr)
        yaml_summary = yaml.dump({'entry': entries}, default_flow_style=False)
        print(yaml_summary)

        for entry in entries:
            response = requests.get(f"{fhir_url}{resource_type}?_id={entry['id']}&", headers=headers)



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
    default=FHIR_BASE,
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
