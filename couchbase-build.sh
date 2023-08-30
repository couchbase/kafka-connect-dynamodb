#!/bin/bash -e

usage() {
    echo "Usage: $0 -p PRODUCT -r RELEASE -v VERSION -b BLD_NUM"
    exit 1
}

while getopts ":p:r:v:b:h?" opt; do
    case $opt in
        p) PRODUCT=$OPTARG ;;
        r) RELEASE=$OPTARG ;;
        v) VERSION=$OPTARG ;;
        b) BLD_NUM=$OPTARG ;;
        h|?) usage ;;
        :) echo "-${OPTARG} requires an argument"
           usage
           ;;
    esac
done

if [ -z "${PRODUCT}" -o -z "${RELEASE}" -o \
     -z "${VERSION}" -o -z "${BLD_NUM}" ]; then
    usage
fi

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "${SCRIPT_DIR}"

PRODUCT_VERSION="${VERSION}.${BLD_NUM}"
./gradlew -Pversion=${PRODUCT_VERSION} shadowJar

# Place desired output jars into dist/ directory at root of repo sync.
# Update this script if the set of desired jars change.
DIST_DIR="$(pwd)/../dist"
mkdir -p "${DIST_DIR}"
cp build/libs/kafka-connect-dynamodb-${PRODUCT_VERSION}.jar "${DIST_DIR}"
