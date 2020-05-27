#!/bin/bash

type cs >/dev/null 2>&1 ||
  (mkdir -p bin &&
    curl -fLo bin/cs https://git.io/coursier-cli-linux &&
    chmod +x bin/cs) &&
  cs setup --apps ammonite,bloop -y
