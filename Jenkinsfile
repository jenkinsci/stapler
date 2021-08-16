node('maven') {
    stage('Checkout') {
        checkout scm   
    }
    stage('Build / Test') {
        sh 'mvn -ntp -Dset.changelist -Dmaven.test.failure.ignore install'
        junit '**/target/surefire-reports/TEST-*.xml'
        infra.prepareToPublishIncrementals()
    }
}
infra.maybePublishIncrementals()
