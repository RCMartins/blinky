#!/bin/sh

export PATH="$PATH:$(pwd)/bin"
type amm >/dev/null 2>&1 || sudo sh -c '(echo "#!/usr/bin/env sh" && curl -L https://github.com/lihaoyi/Ammonite/releases/download/2.1.4/2.12-2.1.4) > /usr/local/bin/amm && chmod +x /usr/local/bin/amm'
export PATH="$PATH:$HOME/.local/share/coursier/bin"
coursier install bloop --only-prebuilt=true
