import json
from pathlib import Path

import click
import os


@click.command()
@click.argument('full_path')
@click.argument('project_name')
@click.option('--input-source', default='https://storage.googleapis.com/fhir-aggregator-public', help='The publicly available https:// url base')
def bulk_import(full_path, project_name, input_source):
    """
    Create manifest for loading FHIR data from bucket.

    \b
    Arguments:\n
    full_path (str): The source of the FHIR ndjson files in the local file system.
    project_name (str): The path in the bucket.
    """

    if not os.path.exists(full_path):
        raise FileNotFoundError(f"{full_path} does not exist")
    if input_source.endswith("/"):
        input_source = input_source[:-1]

    # read all ndjson recursively files in the directory
    ndjson_files = []

    parameters = {
        "resourceType": "Parameters",
        "parameter": [
            {
                "name": "inputFormat",
                "valueCode": "application/fhir+ndjson"
            },
            {
                "name": "inputSource",
                "valueUri": f"{input_source}/{project_name}/META/"
            },
            {
                "name": "storageDetail",
                "part": [
                    {
                        "name": "type",
                        "valueCode": "https"
                    }
                ]
            }
        ]
    }

    for root, dirs, files in os.walk(full_path):
        for file in files:
            if file.endswith(".ndjson"):
                ndjson_files.append(os.path.join(root, file))
    # read each ndjson file and send it to the bulk import endpoint
    for ndjson_file in ndjson_files:
        path = Path(ndjson_file)
        _ = {
          "name": "input",
          "part": [
            {
              "name": "type",
              "valueCode": path.name.split(".")[0]
            },
            {
              "name": "url",
              "valueUri": f"https://storage.googleapis.com/fhir-aggregator-public/{project_name}/META/{path.name}"
            }
          ]
        }
        parameters['parameter'].append(_)

    # Write the JSON output to a file
    output_file = f"bulk-import-request-{project_name}.json"
    with open(output_file, 'w') as f:
        json.dump(parameters, f, indent=2)
    print(f"Manifest written to {output_file}")

if __name__ == '__main__':
    bulk_import()
