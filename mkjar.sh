#!/bin/bash
rm -f files-backup.jar
cd bin;
jar cfe ../files-backup.jar fr.jcharles.files.backup.Main `find ./ -name *.class`  

