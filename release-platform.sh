#!/usr/bin/env sh

current_version=$(cat version)
last_commit=$(git rev-parse HEAD)
meta_file="{:commit \"${last_commit}\" :version \"${current_version}\"}"

cp CHANGELOG.md resources
echo ${meta_file} > resources/platform-meta.edn

