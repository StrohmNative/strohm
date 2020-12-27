#!/bin/bash

set -eo pipefail

test -d .strohm || mkdir .strohm
cd .strohm

password='dev-only'

openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -sha256 -nodes -subj '/CN=localhost/O=Strohm Dev Server'

openssl pkcs12 -export -in cert.pem -inkey key.pem -out localhost.pfx -passout pass:${password}

#keytool -importcert -file cert.pem -keystore dev.keystore -storepass 'welkom123' -noprompt
keytool -importkeystore -destkeystore keystore.jks -srcstoretype PKCS12 -srckeystore localhost.pfx -deststorepass ${password} -srcstorepass ${password}
