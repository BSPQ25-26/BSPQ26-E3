pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        // Adjust these if your tests require a real database or Supabase instance.
        DB_URL                  = 'jdbc:postgresql://localhost:5432/test'
        DB_USERNAME             = 'test'
        DB_PASSWORD             = 'test'
        SUPABASE_URL            = 'https://placeholder.supabase.co'
        SUPABASE_ANON_KEY       = 'placeholder'
        SUPABASE_SERVICE_ROLE_KEY = 'placeholder'
    }

    stages {

        stage('Verify Environment') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'java -version'
                        sh 'mvn -version'
                        sh 'node -v'
                        sh 'npm -v'
                    } else {
                        bat 'java -version'
                        bat 'mvn -version'
                        bat 'node -v'
                        bat 'npm -v'
                    }
                }
            }
        }

        stage('Backend Compile & Test') {
            steps {
                dir('backend') {
                    script {
                        if (isUnix()) {
                            sh 'mvn -B clean test jacoco:report'
                        } else {
                            bat 'mvn -B clean test jacoco:report'
                        }
                    }
                }
            }
        }

        stage('Frontend Build') {
            steps {
                dir('frontend') {
                    script {
                        if (isUnix()) {
                            sh 'npm install'
                            sh 'npm run build'
                        } else {
                            bat 'npm install'
                            bat 'npm run build'
                        }
                    }
                }
            }
        }

        stage('Backend Package') {
            steps {
                dir('backend') {
                    script {
                        if (isUnix()) {
                            sh 'mvn -B package -DskipTests'
                        } else {
                            bat 'mvn -B package -DskipTests'
                        }
                    }
                }
            }
        }

        // Docker Build requires the Docker socket to be mounted into the Jenkins container.
        // Uncomment this stage only if you started Jenkins with -v /var/run/docker.sock:/var/run/docker.sock
        // stage('Docker Build') {
        //     steps {
        //         script {
        //             if (isUnix()) {
        //                 sh 'docker build -t bspq26-e3:latest .'
        //             } else {
        //                 bat 'docker build -t bspq26-e3:latest .'
        //             }
        //         }
        //     }
        // }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml'

            archiveArtifacts artifacts: '''
                backend/target/*.jar,
                backend/target/site/jacoco/**,
                frontend/build/**,
                backend/target/surefire-reports/**
            ''', allowEmptyArchive: true
        }

        success {
            echo 'Pipeline completed successfully: backend compiled, tested, packaged and frontend built.'
        }

        failure {
            echo 'Pipeline failed. Check test results, JaCoCo report, compilation or build errors.'
        }
    }
}
