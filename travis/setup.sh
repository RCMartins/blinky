#!/bin/sh

mkdir -p ~/bin &&
  curl -fLo ~/bin/cs https://git.io/coursier-cli-linux &&
  chmod +x ~/bin/cs &&
  ~/bin/cs setup --apps ammonite,bloop --yes
