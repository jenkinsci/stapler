node('docker') {
    deleteDir()
    checkout scm
    docker.image('maven:3.5-jdk-8').inside {
        sh "MAVEN_OPTS=-Xmx1024m mvn -B -Dmaven.repo.local=${pwd tmp: true}/m2repo -Dmaven.test.failure.ignore -Dset.changelist -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn clean install site"
    }
    archiveArtifacts '**/target/**/*-rc*/'
    junit '**/target/surefire-reports/*.xml'
}
infra.maybePublishIncrementals()
