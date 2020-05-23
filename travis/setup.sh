#!/bin/sh

curl -fLo cs https://git.io/coursier-cli-linux &&
chmod +x cs &&
export PATH="$PATH:$HOME/.local/share/coursier/bin"
cs setup --apps ammonite,bloop --yes
