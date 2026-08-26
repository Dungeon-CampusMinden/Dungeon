#!/usr/bin/env bash
set -euo pipefail

wizard_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repository_dir="$(cd -- "$wizard_dir/.." && pwd)"

cd "$repository_dir"
./gradlew :wizard:buildWizardAuthoringJar --console=plain
exec java -jar wizard/build/libs/DungeonWizard.jar
