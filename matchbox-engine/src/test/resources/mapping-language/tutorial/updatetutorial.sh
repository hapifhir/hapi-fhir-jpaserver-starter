#!/bin/bash

# Source directory
SRC_DIR="../../../../../../../fhir-mapping-tutorial/maptutorial/"

# Destination directory
DEST_DIR="."

# Copy all .java files
rsync -av --include '*/' --include '*.json' --include '*.xml' --include '*.map' --exclude '*' "$SRC_DIR" "$DEST_DIR"
