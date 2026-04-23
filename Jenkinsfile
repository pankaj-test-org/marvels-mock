pipeline {
    agent any

    environment {
        // Centralized environment variable configuration
        JENKINS_FAIL_BUILD = 'false'
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    echo '=== Environment Configuration ==='
                    echo "JENKINS_FAIL_BUILD: ${env.JENKINS_FAIL_BUILD}"
                    def shouldFail = env.JENKINS_FAIL_BUILD == 'true'
                    echo "Build will ${shouldFail ? 'FAIL' : 'PASS'}"

                    // Set image tag with build number
                    env.IMAGE_TAG = "1.0.${env.BUILD_NUMBER}"
                    echo "Image tag: ${env.IMAGE_TAG}"
                }
            }
        }

        stage('Parallel Build & Test') {
            parallel {
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

        stage('Build Docker Image') {
            steps {
                script {
                    echo "Building Docker image with tag: ${env.IMAGE_TAG}"
                    sh "docker build -t pankajydev/jenkins-test-image:${env.IMAGE_TAG} ."

                    // Get the image digest (SHA256 hash)
                    def imageDigest = sh(script: "docker inspect --format='{{index .RepoDigests 0}}' pankajydev/jenkins-test-image:${env.IMAGE_TAG} || docker inspect --format='{{.Id}}' pankajydev/jenkins-test-image:${env.IMAGE_TAG} | cut -d: -f2", returnStdout: true).trim()
                    env.IMAGE_DIGEST = imageDigest
                    echo "Docker image built successfully"
                    echo "Image digest: ${env.IMAGE_DIGEST}"
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    echo 'Pushing Docker image to Docker Hub...'
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'
                        sh "docker push pankajydev/jenkins-test-image:${env.IMAGE_TAG}"
                    }

                    // Get the digest after push (RepoDigests will be available)
                    def pushedDigest = sh(script: "docker inspect --format='{{index .RepoDigests 0}}' pankajydev/jenkins-test-image:${env.IMAGE_TAG} | cut -d@ -f2", returnStdout: true).trim()
                    env.IMAGE_DIGEST = pushedDigest
                    echo "Docker image pushed successfully"
                    echo "Pushed image digest: ${env.IMAGE_DIGEST}"
                }
            }
        }

        stage('Registering build artifact') {
            steps {
                script {
                    echo 'Registering the metadata'
                    echo 'Another echo to make the pipeline a bit more complex'
                    echo "DEBUG: Branch: ${env.BRANCH_NAME}, Change ID: ${env.CHANGE_ID}, Change Branch: ${env.CHANGE_BRANCH}"

                    // Log build causes
                    def causes = currentBuild.getBuildCauses()
                    echo "DEBUG: Build Causes:"
                    causes.each { cause ->
                        echo "  - ${cause}"
                    }

                    def artifactOutput = registerBuildArtifactMetadata(
                        name: "jenkins-test-image",
                        version: "${env.IMAGE_TAG}",
                        type: "docker",
                        url: "docker.io/pankajydev/jenkins-test-image:${env.IMAGE_TAG}",
                        digest: "${env.IMAGE_DIGEST}",
                        label: "pan1"
                    )
                    echo "Artifact output is: ${artifactOutput}"
                    env.ARTIFACT_ID = artifactOutput
                }
            }
        }

        stage('Deploy') {
            steps {
                echo "Artifact ID : ${env.ARTIFACT_ID}"
                registerDeployedArtifactMetadata(
                    artifactId: "${env.ARTIFACT_ID}",
                    artifactUrl: "docker.io/pankajydev/jenkins-test-image:${env.IMAGE_TAG}",
                    targetEnvironment: "pan101",
                    labels: "pan1"
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
