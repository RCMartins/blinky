#!/bin/sh

export PATH="$PATH:$(pwd)/bin:$HOME/.local/share/coursier/bin"
coursier setup --apps ammonite,bloop --yes
