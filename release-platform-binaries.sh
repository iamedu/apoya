#!/usr/bin/env zsh

current_version=$(cat version)

./release-platform.sh

lein libdir
lein jar

rm -rf dist
mkdir dist
mkdir dist/libs
mkdir dist/boot-libs
mkdir dist/bin
mkdir dist/logs
mkdir dist/tmp

cp -r deps/* dist/libs
cp -r lib/* dist/boot-libs
cp -r data dist
cp -r ssl dist

cp target/apoya-${current_version}.jar dist/libs

BIN_SCRIPT=$(cat bin-templates/init)
BIN_SCRIPT=${BIN_SCRIPT/_VERSION_/$current_version}

echo ${BIN_SCRIPT} > dist/bin/apoya
chmod +x dist/bin/apoya

rm -rf dist/data/fs/sites/*/bower_components
lein apoya-minify
cp -r data/fs/sites/default/bower_components/font-awesome/fonts dist/data/fs/sites/default/bower_components/font-awesome/fonts

mv app-dist/sites/default/bower_components dist/data/fs/sites/default/
rm -rf app-dist

