node('docker') {
    deleteDir()
    docker.image('maven:3.5-jdk-8').inside {
        sh "MAVEN_OPTS=-Xmx1024m mvn -B -Dmaven.repo.local=${pwd tmp: true}/m2repo -Dmaven.test.failure.ignore -Dset.changelist clean install site"
    }
    archiveArtifacts '**/target/**/*-rc*/'
    junit '**/target/surefire-reports/*.xml'
}
infra.maybePublishIncrementals()
