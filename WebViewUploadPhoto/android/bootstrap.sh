#!/bin/sh

BASEDIR=$( (cd -P "`dirname "$0"`" && pwd) )

android update project \
    --path "$BASEDIR" \
    --name "UploadPhoto" \
    --target "android-16" || exit 1
    
 



