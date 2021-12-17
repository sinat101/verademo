/*
 * Normal Jenkinsfile that will build and do Policy and SCA scans
 */

pipeline {
    agent any

    environment {
        VERACODE_APP_NAME = 'Verademo'      // App Name in the Veracode Platform
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

    stages{
        stage ('environment verify') {
            steps {
                script {
                    if (isUnix() == true) {
                        sh 'pwd'
                        sh 'ls -la'
                        sh 'echo $PATH'
                    }
                    else {
                        bat 'dir'
                        bat 'echo %PATH%'
                    }
                }
            }
        }

        stage ('Veracode SCA') {
            steps {
                echo 'Veracode SCA'
                withCredentials([ string(credentialsId: 'SCA_Token', variable: 'SRCCLR_API_TOKEN')]) {
                    withMaven(maven:'maven-3') {
                        script {
                            if(isUnix() == true) {
                                sh '''
                                    export SCAN_DIR="./app"
                                    touch SCA_Results_Build_${BUILD_NUMBER}.txt
                                    curl -sSL https://download.sourceclear.com/ci.sh | bash -s -- scan --update-advisor 2>&1 | tee SCA_Results_Build_${BUILD_NUMBER}.txt
                                    grep -E 'CVE-2016-1000031|CVE-2021-22096' SCA_Results_Build_${BUILD_NUMBER}.txt
                                '''

                                // debug, no upload
                                //sh "curl -sSL https://download.sourceclear.com/ci.sh | DEBUG=1 sh -s -- scan --no-upload"
                            }
                            else {
                                powershell '''
                                            Set-ExecutionPolicy AllSigned -Scope Process -Force
                                            $ProgressPreference = "silentlyContinue"
                                            iex ((New-Object System.Net.WebClient).DownloadString('https://download.srcclr.com/ci.ps1'))
                                            srcclr scan app/
                                            '''
                            }
                        }
                    }
                }
            }
        }
    }
}
