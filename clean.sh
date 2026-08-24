#!/usr/bin/env bash
set -euo pipefail

echo "Cleaning generated files..."
rm -f ./*.class ./Output.java
rm -rf ./build
echo "Clean complete. Source files were not changed."
