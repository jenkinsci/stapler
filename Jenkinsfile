node('docker') {
    deleteDir()
    checkout scm
    docker.image('maven:3.6.0-jdk-8').inside {
        withEnv(['MAVEN_OPTS=-Xmx1024m', "TMP=${pwd tmp: true}"]) {
            sh '''
                alias _mvn='mvn -B -Dmaven.repo.local=$TMP/m2repo -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -e'
                _mvn -Dmaven.test.failure.ignore -Dset.changelist clean install
                _mvn -DskipTests install site
            '''
        }
    }
    archiveArtifacts '**/target/**/*-rc*/'
    junit '**/target/surefire-reports/*.xml'
}
infra.maybePublishIncrementals()
