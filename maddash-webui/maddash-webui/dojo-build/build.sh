#!/bin/bash
APP_NAME="maddash";
DOJO_VERSION="1.7.2";
BUILD_PROFILE="../../../maddash.profile.js";
TARGET_PATH="../../web/lib";

if [ ! -f dojo-release-$DOJO_VERSION-src.tar.gz ] && [ ! -d dojo-release-$DOJO_VERSION-src ]; then
    echo "-- Unpacking Dojo $DOJO_VERSION source...";
    wget "http://download.dojotoolkit.org/release-$DOJO_VERSION/dojo-release-$DOJO_VERSION-src.tar.gz";
    if [ $? != 0 ]; then
        echo "-- Error occurred while downloading dojo source.";
        exit 1;
    fi
fi

if [ -f dojo-release-$DOJO_VERSION-src.tar.gz ] && [ ! -d dojo-release-$DOJO_VERSION-src ]; then
    echo "-- Unpacking dojo-release-$DOJO_VERSION-src.tar.gz";
    gunzip dojo-release-$DOJO_VERSION-src.tar.gz;
    if [ $? != 0 ]; then
        echo "-- Error occurred unzipping dojo.";
        exit 1;
    fi
fi

if [ -f dojo-release-$DOJO_VERSION-src.tar ] && [ ! -d dojo-release-$DOJO_VERSION-src ]; then
    echo "-- Unpacking dojo-release-$DOJO_VERSION-src.tar";
    tar -xvf dojo-release-$DOJO_VERSION-src.tar;
    if [ $? != 0 ]; then
        echo "-- Error occurred untarring dojo.";
        `rm dojo-release-$DOJO_VERSION-src.tar`;
        exit 1 ;
    fi
    `rm dojo-release-$DOJO_VERSION-src.tar`;
    # Removing files we do not use and that contain potential privacy breach
    # See https://lintian.debian.org/tags/privacy-breach-google-adsense
    rm "dojo-release-$DOJO_VERSION-src/dojox/analytics/Urchin.js"
    rm "dojo-release-$DOJO_VERSION-src/dojox/rpc/demos/demo_GoogleAjax.html"
    rm "dojo-release-$DOJO_VERSION-src/dojox/rpc/demos/demo_GoogleAjaxTranslate.html"
fi

echo "-- Building custom dojo package...";
if [ ! -d ./dojo-release-$DOJO_VERSION-src/util/buildscripts/ ]; then
    echo "Could not find directory dojo-release-$DOJO_VERSION-src/util/buildscripts/";
    exit 1;
fi
cd ./dojo-release-$DOJO_VERSION-src/util/buildscripts/;

./build.sh profile="$BUILD_PROFILE" action=release
if [ $? != 0 ]; then
    echo "-- Error building dojo package.";
    exit 1;
fi
echo "-- Custom dojo package built.";

echo "-- Compressing files";
cd ../../release
tar -cf dojo-release-$DOJO_VERSION-$APP_NAME.tar dojo
if [ $? != 0 ]; then
    echo "-- Error creating dojo-release-$DOJO_VERSION-$APP_NAME.tar";
    exit 1;
fi
gzip dojo-release-$DOJO_VERSION-$APP_NAME.tar
if [ $? != 0 ]; then
    echo "-- Error creating dojo-release-$DOJO_VERSION-$APP_NAME.tar.gz";
    exit 1;
fi

echo "-- Installing new dojo files";
cd ../
if [ -d $TARGET_PATH/dojo/ ]; then
    echo "--- Removing old dojo files";
    rm -rf $TARGET_PATH/dojo/;
fi

cp -r release/dojo $TARGET_PATH/dojo
if [ $? != 0 ]; then
    echo "-- Error copying dojo package.";
    exit 1;
fi

mv release/dojo-release-$DOJO_VERSION-$APP_NAME.tar.gz $TARGET_PATH
if [ $? != 0 ]; then
    echo "-- Error copying dojo-release-$DOJO_VERSION-$APP_NAME.tar.gz.";
    exit 1;
fi
echo "-- New dojo files installed.";

echo "";
echo "##############################################################################";
echo "Dojo successfully built!";
echo "##############################################################################";
