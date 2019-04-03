pipeline {
    agent {
      docker 'maven:3.5-jdk-8'
    }

    environment {
      MAVEN_OPTS = "-Xmx1024m"
    }

    stages {
        stage("Build") {
            steps {
              sh 'mvn -B -Dmaven.test.failure.ignore -Dset.changelist clean install site'
            }
        }
    }

    post {
        success {
            archive "**/target/**/*-rc*/"
            junit '**/target/surefire-reports/*.xml'
            script { // TODO figure out how to release the agent before doing this
                infra.maybePublishIncrementals()
            }
        }
    }

}
