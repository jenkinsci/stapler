node('maven') {
    stage('Checkout') {
        checkout scm   
    }
    stage('Build / Test') {
        sh '''
            mvn -ntp -Dset.changelist -Dmaven.test.failure.ignore install
            # Without -Dset.changelist (see 8edc206):
            mvn -ntp -DskipTests install
            mvn -ntp -DskipTests site
        '''
        junit '**/target/surefire-reports/TEST-*.xml'
        infra.prepareToPublishIncrementals()
    }
}
infra.maybePublishIncrementals()
