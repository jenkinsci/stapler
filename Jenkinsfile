node('docker') {
    deleteDir()
    checkout scm
    def tmp = pwd tmp: true
    docker.image('maven:3.6.0-jdk-8').inside {
        withEnv(['MAVEN_OPTS=-Xmx1024m', "TMP=$tmp"]) {
            sh '''
                alias _mvn='mvn -B -Dmaven.repo.local=$TMP/m2repo -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -e'
                _mvn -Dset.changelist help:evaluate -Dexpression=changelist -Doutput=$TMP/changelist clean -Dmaven.test.failure.ignore install
                _mvn -DskipTests install site
            '''
        }
    }
    junit '**/target/surefire-reports/TEST-*.xml'
    def changelist = readFile("$tmp/changelist")
    dir("$tmp/m2repo") {
        archiveArtifacts "**/*$changelist/*$changelist*"
    }
}
infra.maybePublishIncrementals()
