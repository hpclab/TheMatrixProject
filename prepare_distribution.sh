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

# create the empty directories
mkdir $DIR/iad/
mkdir $DIR/results/ 
mkdir $DIR/scripts/

# copy directories to the destination directory
cp -r ./javadoc/ $DIR
cp -r ./lib/ $DIR
cp -r ./lookups/ $DIR
cp -r ./mapping/ $DIR
cp -r ./queries/ $DIR


# copy files to the destination directory
cp MetaXmlCreator $DIR
cp MetaXmlCreatorWIN.bat $DIR
cp settings.xml $DIR
cp TheMatrix $DIR
cp TheMatrixWIN.bat $DIR
cp TheMatrixB.jar $DIR

# zip the directory (delete if already present)
ZIPFILE=$DIR.zip
if [ -f "$ZIPFILE" ]; then
  rm $ZIPFILE
fi

zip -rq $ZIPFILE $DIR

# delete the directory
rm -rf $DIR

echo "************* PROCESS COMPLETED *****************"
