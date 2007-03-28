#!/bin/sh -ex
#maven clean dist source
#maven javanet:deploy-jar javanet:deploy-java-source
# mvn clean deploy
# javanet:dist

ver=$(show-pom-version pom.xml)
javanettasks uploadFile stapler /stapler-$ver.zip "$ver release" stable target/stapler-$ver.zip
