# Just a Makefile for manual testing
.PHONY: all

VERSION = 2.107.3-stapler-it-1.255-SNAPSHOT

all: clean build

clean:
	if [ -f "tmp" ]; then
		rm -rf tmp
	fi

build: tmp/output/target/jenkins-war-${VERSION}.war

tmp/output/target/jenkins-war-${VERSION}.war:
	# TODO: https://github.com/jenkinsci/custom-war-packager/pull/26
	mvn io.jenkins.tools.custom-war-packager:custom-war-packager-maven-plugin:0.1-alpha-6-20180509.154909-1:build -DconfigFile=essentials.yml -Dversion=${VERSION}

run: tmp/output/target/jenkins-war-${VERSION}.war
	JENKINS_HOME=work java -jar tmp/output/target/jenkins-war-${VERSION}.war \
        --httpPort=8080 --prefix=/jenkins

pct: tmp/output/target/jenkins-war-${VERSION}.war
	docker run --rm -v maven-repo:/root/.m2 -v $(pwd)/out:/pct/out \
		-v tmp/output/target/jenkins-war-${VERSION}.war:/pct/jenkins.war:ro \
		-e ARTIFACT_ID=pipeline-maven \
		-e INSTALL_BUNDLED_SNAPSHOTS=true \
		jenkins/pct
