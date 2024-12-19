# README: Bucket Usage Guide

## Overview

This document outlines the conventions and usage policies for the public bucket. The bucket is intended to store **read-only, publicly accessible** files in a structured format. **No Protected Health Information (PHI)** or other sensitive data should be submitted to or stored in this bucket.

---

## Access Policy

- **Public Read-Only:** All objects in the bucket are publicly accessible and read-only.
- **Write Permissions:** Only authorized personnel have permissions to write or modify objects in the bucket.

---

## Object Key (Path) Conventions

The object keys (paths) in the bucket follow a strict naming convention to ensure consistency and discoverability:

> <program-project>/META/<resource>.ndjson



### Breakdown of the Path

1. **`program`**  
   - Represents the consortium or overarching initiative.  
   - Example: `TCGA`, `ICGC`.

2. **`project`**  
   - Denotes the specific dataset, experiment, or subproject within the program.  
   - Example: `LungCancerStudy`, `BreastCancerCohort`.

3. **`META`**  
   - A fixed folder indicating metadata files. This helps segregate metadata resources from other data types.

4. **`resource`**  
   - Represents the type of resource being stored, named after **FHIR object types** in camel case.  
   - Example: `Patient`, `Specimen`, `Observation`.

5. **File Format**  
   - All files must use the `.ndjson` (Newline-Delimited JSON) format to ensure compatibility with FHIR standards.

---

## Examples of Object Keys

- `TCGA-LungCancerStudy/META/Patient.ndjson`  
- `ICGC-BreastCancerCohort/META/Specimen.ndjson`  
- `TCGA-LiverCancerProject/META/Observation.ndjson`  

---

## Guidelines for File Content

1. **File Format:**  
   - Each line in the `.ndjson` file must represent a single JSON object conforming to the respective FHIR resource type.

2. **Data Standards:**  
   - All files must adhere to the FHIR specification for the stated resource type.  

3. **No PHI or Sensitive Data:**  
   - Ensure that no Protected Health Information (PHI) or other sensitive data is included in any files submitted to this bucket.

---

## Compliance and Monitoring

- **Validation Checks:**  
  - Periodic audits will ensure that uploaded files conform to the conventions and contain no sensitive data.
  
- **Report Issues:**  
  - If you encounter any non-compliant files, report them immediately to the bucket administrator for review and removal.

---

## Contact

For questions or issues regarding this bucket, contact the **Cloud DevOps Team** at:  
**Slack:** #ncpi-fhir-aggregator
