#!/bin/bash

##
# Script to build tarball. If 'git archive' ever adds a recursive option we can likely 
# drop this since we only need it to grab the git submodules.
#

#get input
if [ -z "$1" ]; then
    echo "No version specified"
    echo "Usage: ./build_tarball.sh VERSION DESTDIRECTORY"
    exit 1
fi
VERSION=$1
if [ -z "$2" ]; then
    echo "No destination directory specified"
    echo "Usage: ./build_tarball.sh VERSION DESTDIRECTORY"
    exit 1
fi
DSTDIR=$2

#run git archive
TMP=`mktemp -d`
git archive --format=tar --prefix=maddash-${VERSION}/ HEAD | gzip > $TMP/maddash-tmp.tar.gz
cd maddash-server/madalert
git archive --format=tar --prefix=madalert/ HEAD | gzip > $TMP/madalert.tar.gz

#merge tarballs
cd $TMP
tar -xzf maddash-tmp.tar.gz
tar -xzf madalert.tar.gz
mv madalert/* maddash-${VERSION}/maddash-server/madalert/

#move to destination
tar -c  maddash-${VERSION}| gzip > ${DSTDIR}/maddash-${VERSION}.tar.gz

#cleanup
rm -rf $TMP
