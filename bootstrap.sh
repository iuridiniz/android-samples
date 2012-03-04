#!/bin/sh

BASEDIR=$( (cd -P "`dirname "$0"`" && pwd) )

android update project \
    --path "$BASEDIR" \
    --name "Hello World" || exit 1
    
 



