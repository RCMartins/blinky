#!/bin/sh

chmod +x ./bin/coursier
export PATH="$PATH:$(pwd)/bin"
sudo sh -c '(echo "#!/usr/bin/env sh" && curl -L https://github.com/lihaoyi/Ammonite/releases/download/2.1.2/2.12-2.1.2) > /usr/local/bin/amm && chmod +x /usr/local/bin/amm'
export PATH="$PATH:$HOME/.local/share/coursier/bin"
coursier install bloop --only-prebuilt=true
