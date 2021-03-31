#!/usr/bin/env bash

set -x

export JAVA_OPTS='-XX:+IgnoreUnrecognizedVMOptions --add-modules java.xml.bind'
export PATH="$ANDROID_HOME"/tools/bin:$PATH
sdkmanager --install 'ndk;21.4.7075529'  >/dev/null