Contributing
============

**WORK IN PROGRESS**

Release Android Library
-----------------------

Using Fish:

```
set -x ORG_GRADLE_PROJECT_signingKey (gpg --armor --export-secret-key AFC71E0A | string split0)
set -x ORG_GRADLE_PROJECT_signingPassword <<passphrase>>
./gradlew publishToMavenLocal
```
