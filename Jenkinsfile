pipeline {
    agent any

    environment {
        // Centralized environment variable configuration
        JENKINS_FAIL_BUILD = 'false'
        ARTIFACT_ID = "artifact-${BUILD_NUMBER}-${BUILD_TIMESTAMP}"
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    echo '=== Environment Configuration ==='
                    echo "JENKINS_FAIL_BUILD: ${env.JENKINS_FAIL_BUILD}"
                    def shouldFail = env.JENKINS_FAIL_BUILD == 'true'
                    echo "Build will ${shouldFail ? 'FAIL' : 'PASS'}"
                }
            }
        }

        stage('Build') {
            parallel {
                stage('Compile') {
                    steps {
                        echo "Compiling..."
                        echo "Compilation completed"
                    }
                }

                stage('Package') {
                    stages {
                        stage('Package - Step 1') {
                            steps {
                                echo "Packaging - Step 1"
                            }
                        }
                        stage('Package - Step 2') {
                            steps {
                                echo "Packaging - Step 2"
                            }
                        }
                    }
                }
            }
        }

        stage('Parallel Build & Test') {
            parallel {
                stage('Build Verification') {
                    steps {
                        echo "Build Verification"
                    }
                }
                stage('Unit Tests') {
                    steps {
                        echo 'Running unit tests...'
                        echo 'Unit tests completed'
                        script {
                            // Check if build should fail
                            def shouldFail = env.JENKINS_FAIL_BUILD == 'true'
                            if (shouldFail) {
                                echo "⚠️ JENKINS_FAIL_BUILD=true - failing intentionally"
                                error("Build failed intentionally (set JENKINS_FAIL_BUILD=false to pass)")
                            } else {
                                echo "✅ Tests passed"
                            }
                        }
                    }
                }
            }
        }

        stage('Registering build artifact') {
            steps {
                script {
                    echo 'Registering the metadata'
                    echo 'Another echo to make the pipeline a bit more complex'
                    def artifactOutput = registerBuildArtifactMetadata(
                        name: "gha-test-image",
                        version: "1.0.0",
                        type: "docker",
                        url: "docker.io/pankajydev/gha-test-image:1.0.0",
                        digest: "b5efa05e8033481620ea88606b3da1992ec830d141588f97c5a8f98d72683b6b",
                        label: "preprod"
                    )
                    echo "Artifact output is: ${artifactOutput}"
                    env.ARTIFACT_ID = artifactOutput
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Running Unit Tests...'
            }
        }

        stage('Deploy') {
            steps {
                echo "Artifact ID : ${env.ARTIFACT_ID}"
                registerDeployedArtifactMetadata(
                    id: "${env.ARTIFACT_ID}",
                    url: "docker.io/pankajydev/gha-test-image:1.0.0",
                    targetEnvironment: "preprod",
                    labels: "preprod"
                )
                echo 'Deploying...'
            }
        }

        stage('Maven Clean Compile') {
            steps {
                echo 'Starting build stage...'
                sh 'mvn -B clean compile'
                echo 'Build stage completed.'
            }
        }

        stage('Maven Test Run') {
            steps {
                echo 'Starting test stage...'
                sh 'mvn -B test'
                echo 'Test stage completed.'
            }
        }

        stage('Archive Test Results') {
            steps {
                echo 'Archiving test results...'
                junit '**/target/surefire-reports/*.xml'
                echo 'Test results archived.'
            }
        }

        stage('Archive artifacts') {
            steps {
                echo 'Compiling the project...'
                sh 'mkdir -p target && echo "dummy jar content" > target/app.jar'
                archiveArtifacts artifacts: 'target/*.jar'
                echo 'Artifact generated: target/app.jar'
            }
        }

        stage('Security Scan') {
            steps {
                sh "echo 'Security scan with multiple results'"
                sh "pwd"
                sh "echo 'Security scan result for security-scan-results-s8-a.sarif' > security-scan-results-s8-a.sarif && ls -l security-scan-results-s8-a.sarif"
                sh "echo 'Security scan result for security-scan-results-s8-b.sarif' > security-scan-results-s8-b.sarif && ls -l security-scan-results-s8-b.sarif"
                registerSecurityScan artifacts: "security-scan-results-s8-*.sarif", format: "sarif", scanner: "sonarqube"
            }
        }
    }

    post {
        always {
            echo "Pipeline finished with status: ${currentBuild.currentResult}"
        }
    }
}
