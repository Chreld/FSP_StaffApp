#!/bin/bash

adb wait-for-device
adb reverse tcp:9090 tcp:9090

echo "ADB reverse configured!"