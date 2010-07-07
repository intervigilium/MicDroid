#!/bin/bash

zipalign -v 4 MicDroid.apk MicDroid-aligned.apk
mv MicDroid-aligned.apk MicDroid.apk
