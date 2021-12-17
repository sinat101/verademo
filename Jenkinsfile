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

        // stage ('build') {
        //     steps {
        //         withMaven(maven:'maven-3') {
        //             script {
        //                 if(isUnix() == true) {
        //                     sh 'mvn clean package'
        //                 }
        //                 else {
        //                     bat 'mvn clean package'
        //                 }
        //             }
        //         }
        //     }
        // }

        // stage ('Veracode scan') {
        //     steps {
        //         script {
        //             if(isUnix() == true) {
        //                 env.HOST_OS = 'Unix'
        //             }
        //             else {
        //                 env.HOST_OS = 'Windows'
        //             }
        //         }

        //         echo 'Veracode scanning'
        //         withCredentials([ usernamePassword ( 
        //             credentialsId: 'veracode_login', usernameVariable: 'VERACODE_API_ID', passwordVariable: 'VERACODE_API_KEY') ]) {
        //                 // fire-and-forget 
        //                 veracode applicationName: "${VERACODE_APP_NAME}", criticality: 'VeryHigh', debug: true, fileNamePattern: '', pHost: '', pPassword: '', pUser: '', replacementPattern: '', sandboxName: '', scanExcludesPattern: '', scanIncludesPattern: '', scanName: "${BUILD_TAG}-${env.HOST_OS}", uploadExcludesPattern: '', uploadIncludesPattern: 'target/verademo.war', vid: "${VERACODE_API_ID}", vkey: "${VERACODE_API_KEY}"

        //                 // wait for scan to complete (timeout: x)
        //                 //veracode applicationName: '${VERACODE_APP_NAME}'', criticality: 'VeryHigh', debug: true, timeout: 20, fileNamePattern: '', pHost: '', pPassword: '', pUser: '', replacementPattern: '', sandboxName: '', scanExcludesPattern: '', scanIncludesPattern: '', scanName: "${BUILD_TAG}", uploadExcludesPattern: '', uploadIncludesPattern: 'target/verademo.war', vid: '${VERACODE_API_ID}', vkey: '${VERACODE_API_KEY}'
        //             }      
        //     }
        // }

        stage ('Veracode SCA') {
            steps {
                echo 'Veracode SCA'
                withCredentials([ string(credentialsId: 'SCA_Token', variable: 'SRCCLR_API_TOKEN')]) {
                    withMaven(maven:'maven-3') {
                        script {
                            if(isUnix() == true) {
                                sh "curl -sSL https://download.sourceclear.com/ci.sh | sh"

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
