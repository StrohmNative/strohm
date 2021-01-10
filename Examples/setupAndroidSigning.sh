#!/bin/bash

set -eo pipefail

scriptDir=$(cd `dirname $0` && pwd)

mkdir -p ${scriptDir}/Signing

keytool -genkey -v \
  -keystore Signing/examples.keystore \
  -storepass welcome123 \
  -alias counter -keyalg RSA -keysize 2048 -validity 30 \
  -dname "cn=Examples,o=Examples,c=NL"
