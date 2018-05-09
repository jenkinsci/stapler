pipeline {
    agent {
      docker 'maven:3.3.9-jdk-8'
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
        stage("Integration Tests") {
            steps {
              incrementalsTest(baseDir: "src/test/it")
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
