pipeline {
    agent label:"docker", docker: "maven:3.3.9-jdk-7"

    environment {
      MAVEN_OPTS = "-XX:MaxPermSize=256m -Xmx1024m"
    }

    stages {
        stage("Build") {
            sh 'mvn clean install -Dmaven.test.failure.ignore=true'
        }
    }

    postBuild {
        always {
            archive "**/target/**/*.jar"
            junit '**/target/surefire-reports/*.xml'
        }
    }

}
