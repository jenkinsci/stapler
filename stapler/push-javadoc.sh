#!/bin/sh -ex
mvn javadoc:javadoc
cp -R target/site/apidocs/* ../www/javadoc
