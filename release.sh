#!/usr/bin/env sh

last_commit=$(git rev-parse HEAD)
meta_file="{:commit ${last_commit}}"

echo ${meta_file} > data/fs/classpath/meta.edn
jar cfv app-${last_commit}.far -C data/fs/classpath .
rm data/fs/classpath/meta.edn

