import json
from pathlib import Path

import click
import os


@click.group()
def cli():
    pass


@cli.command()
@click.option(
    '--directory', '-d',
    type=click.Path(exists=True, file_okay=False),
    required=True,
    help="Path to the directory containing NDJSON files."
)
def bulk_import(directory):
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
                "valueUri": "https://fhir-aggregator-public.s3.us-west-2.amazonaws.com"
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

    for root, dirs, files in os.walk(directory):
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
              "valueUri": f"https://fhir-aggregator-public.s3.us-west-2.amazonaws.com/{path}"
            }
          ]
        }
        parameters['parameter'].append(_)
    print(json.dumps(parameters))


if __name__ == '__main__':
    cli()
