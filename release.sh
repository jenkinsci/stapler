#!/bin/sh -ex
mvn -B release:prepare release:perform
ver=$(show-pom-version target/checkout/pom.xml)
javanettasks uploadFile stapler /stapler-$ver.zip "$ver release" stable target/checkout/target/stapler-$ver-bin.zip
./push-javadoc.sh