#!/bin/sh
# Got from:
# http://stackoverflow.com/questions/4567904/how-to-start-an-application-using-android-adb-tools
# http://www.android.pk/blog/general/launch-app-through-adb-shell/
ACTION="android.intent.action.MAIN"
APP="com.igdium.helloworld"
ACTIVITY=".HelloWorldAndroidActivity"

#adb shell am start -a "$ACTION" -n "$APP"/"$ACTIVITY"
adb shell am start -n "$APP"/"$ACTIVITY"
