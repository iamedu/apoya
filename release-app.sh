#!/usr/bin/env sh

current_version=$(cat app-version)
app_name=$(cat app-name)
last_commit=$(git rev-parse HEAD)
meta_file="{:commit \"${last_commit}\" :version \"${current_version}\" :app-name \"${app_name}\"}"

rm -rf app-dist
mkdir app-dist
mkdir app-dist/classpath

cp -r data/fs/* app-dist
rm -rf app-dist/sites/*/bower_components

lein apoya-minify
cp -r data/fs/sites/default/bower_components/font-awesome/fonts app-dist/sites/default/bower_components/font-awesome
cp -r data/fs/sites/default/bower_components/FileAPI app-dist/sites/default/bower_components

echo ${meta_file} > app-dist/classpath/app-meta.edn
cp APP_CHANGELOG.md app-dist/classpath/

jar cfv app-${current_version}.far -C app-dist .

rm -rf app-dist

