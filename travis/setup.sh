#!/bin/sh

curl -fLo cs https://git.io/coursier-cli-linux &&
  chmod +x cs &&
  cs setup --apps ammonite,bloop --yes
