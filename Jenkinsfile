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
              sh 'mvn -B -Dmaven.test.failure.ignore clean install site'
            }
        }
    }

    post {
        success {
            archive "**/target/**/*.jar"
            junit '**/target/surefire-reports/*.xml'
        }
    }

}
