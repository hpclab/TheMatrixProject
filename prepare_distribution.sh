#!/bin/bash

DIR=$"./TheMatrixDistribution"

if [ -d "$DIR" ]; then
  rm -rf $DIR
fi
mkdir $DIR

# compile the project and create the jar
ant clean
ant build
ant -f build-jar.xml

# compile the javadoc
ant -f build-javadoc.xml

# copy directories to the destination directory
cp -r ./javadoc $DIR
cp -r ./iad $DIR
cp -r ./lib $DIR
cp -r ./lookups $DIR
cp -r ./mapping $DIR
cp -r ./queries $DIR
cp -r ./results $DIR
cp -r ./scripts $DIR

# copy files to the destination directory
cp MetaXmlCreator $DIR
cp MetaXmlCreatorWIN.bat $DIR
cp settings.xml $DIR
cp TheMatrix $DIR
cp TheMatrixWIN.bat $DIR
cp TheMatrixB.jar $DIR

# zip the directory
zip TheMatrixDistribution.zip $DIR/*

# delete the directory
rm -rf $DIR

echo "************* PROCESS COMPLETED *****************"
