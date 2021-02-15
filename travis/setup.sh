#!/bin/sh

# Temporary fix the weird bug with downloading this library...
mkdir -p ~/.cache/coursier/v1/https/repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.64
curl https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.64/bcprov-jdk15on-1.64.pom > ~/.cache/coursier/v1/https/repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.64/bcprov-jdk15on-1.64.pom
curl https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.64/bcprov-jdk15on-1.64.jar > ~/.cache/coursier/v1/https/repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.64/bcprov-jdk15on-1.64.jar
# ----------

type cs >/dev/null 2>&1 ||
  (mkdir -p bin &&
    curl -fLo bin/cs https://git.io/coursier-cli-linux &&
    chmod +x bin/cs) &&
  cs setup --apps ammonite,bloop -y
