pipeline {
    agent {
      label 'linux'
    }

    environment {
      MAVEN_OPTS = "-Xmx1024m"
    }

    stages {
        stage("Build") {
            steps {
                script {
                    infra.runMaven(["-Dmaven.test.failure.ignore", "clean", "install", "site"])
                }
            }
        }
        stage("Integration Tests") {
            steps {
                script {
                    essentialsTest(baseDir: "src/test/it")
                }
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
