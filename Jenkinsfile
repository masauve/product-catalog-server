pipeline {
    agent {
        //label 'quarkus'
        label 'maven'
    }
    stages {

        stage('Build Server App') {
            steps {
                git branch: 'master', url: 'https://github.com/gnunn1/product-catalog-server'
                sh "env | grep -i JAVA"
                sh "echo ${JAVA_TOOL_OPTIONS}"
                //sh "mvn package -Pnative -e -B -DskipTests -Dmaven.javadoc.skip=true -Dmaven.site.skip=true -Dmaven.source.skip=true -Djacoco.skip=true -Dcheckstyle.skip=true -Dfindbugs.skip=true -Dpmd.skip=true -Dfabric8.skip=true"
                sh "./mvnw package -DskipTests -Dmaven.javadoc.skip=true -Dmaven.site.skip=true -Dmaven.source.skip=true -Djacoco.skip=true -Dcheckstyle.skip=true -Dfindbugs.skip=true -Dpmd.skip=true -Dfabric8.skip=true"
            }
        }
        stage('Build Server Image') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject("product-catalog-dev") {
                        //openshift.selector("bc", "server").startBuild("--from-file=target/product-catalog-1.0-SNAPSHOT-runner", "--wait=true")
                        openshift.selector("bc", "server").startBuild("--from-file=target/product-catalog-1.0-SNAPSHOT-runner.jar", "--wait=true")
                        }
                    }
                }
            }
        }
        stage('Deploy Server Image') {
            steps {
                sh "oc patch deployment server -n product-catalog-dev -p \"{\\\"spec\\\":{\\\"template\\\":{\\\"metadata\\\":{\\\"labels\\\":{\\\"date\\\":\\\"`date +'%s'`\\\"}}}}}\""
                sh "oc rollout status deployment server -n product-catalog-dev"
            }
        }

        stage('Approve') {
            steps {
            timeout(time:15, unit:'MINUTES') {
                input message: "Promote to Test?", ok: "Promote"
            }
            }
        }

        stage('Deploy Images to Test') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject('product-catalog-test') {
                        openshift.tag("product-catalog-dev/server:latest", "product-catalog-test/server:latest")
                        }
                    }
                }
                sh "oc patch deployment server -n product-catalog-test -p \"{\\\"spec\\\":{\\\"template\\\":{\\\"metadata\\\":{\\\"labels\\\":{\\\"date\\\":\\\"`date +'%s'`\\\"}}}}}\""
                sh "oc rollout status deployment server -n product-catalog-test"
            }
        }
    }
}