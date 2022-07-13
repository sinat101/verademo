/*
 * Normal Jenkinsfile that will build and do Policy and SCA scans
 */

pipeline {
    agent any

    environment {
        VERACODE_APP_NAME = 'VeraDemo'      // App Name in the Veracode Platform
    }

    // this is optional on Linux, if jenkins does not have access to your locally installed docker
    //tools {
        // these match up with 'Manage Jenkins -> Global Tool Config'
        //'org.jenkinsci.plugins.docker.commons.tools.DockerTool' 'docker-latest' 
    //}

    options {
        // only keep the last x build logs and artifacts (for space saving)
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20'))
    }

    stages {

        stage ('build') {
            steps {
                withMaven(maven:'maven-3') {
                    script {
                        dir('app') {
                            sh 'mvn clean package'
                        }
                    }
                }
            }
        }

        stage ('Pipeline Scan') {
            steps {
                echo 'Pipeline Scan'
                withCredentials([ usernamePassword (
                    credentialsId: 'veracode_login', usernameVariable: 'VERACODE_API_ID', passwordVariable: 'VERACODE_API_KEY') ]) {
                        sh 'curl -sSO https://downloads.veracode.com/securityscan/pipeline-scan-LATEST.zip'
                        unzip zipFile: 'pipeline-scan-LATEST.zip'
                        sh 'java -jar pipeline-scan.jar -vid ${VERACODE_API_ID} -vkey ${VERACODE_API_KEY} -f app/target/verademo.war -fs="Very High"'
                }
            }
        }

        stage ('Veracode SCA') {
            steps {
                echo 'Veracode SCA'
                withCredentials([ string(credentialsId: 'SCA_Token', variable: 'SRCCLR_API_TOKEN')]) {
                    withMaven(maven:'maven-3') {
                        script {
                            sh '''
                                export SCAN_DIR="./app"
                                curl -sSL https://download.sourceclear.com/ci.sh | bash -s scan --update-advisor
                            '''
                        }
                    }
                }
            }
        }
    }
}
