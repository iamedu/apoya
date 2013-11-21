#!/usr/bin/env sh

current_version=$(cat app-version)
last_commit=$(git rev-parse HEAD)
meta_file="{:commit \"${last_commit}\" :version \"${current_version}\"}"

rm -rf app-dist
mkdir app-dist

cp APP_CHANGELOG.md app-dist
echo ${meta_file} > app-dist/platform-meta.edn

cp -r data/fs/* app-dist
rm -rf app-dist/sites/*/bower_components

jar cfv app-${current_version}.far -C app-dist .

rm -rf app-dist

