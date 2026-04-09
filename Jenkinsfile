// Jenkinsfile for Generic Webhook Trigger + GitHub Status Reporter
// This setup allows GitHub Actions to trigger Jenkins without Jenkins API tokens

pipeline {
    agent any

    options {
        // Keep builds for 30 days
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }

    // These parameters will be populated by Generic Webhook Trigger plugin
    parameters {
        string(name: 'repository', defaultValue: '', description: 'GitHub repository (owner/repo)')
        string(name: 'sha', defaultValue: '', description: 'Commit SHA')
    }

    stages {
        stage('Set GitHub Status - Pending') {
            steps {
                script {
                    // Report pending status to GitHub using API
                    if (params.sha && params.repository) {
                        try {
                            withCredentials([string(credentialsId: 'github-status-token', variable: 'GITHUB_TOKEN')]) {
                                sh """
                                    curl -f -X POST \
                                      -H "Authorization: token \${GITHUB_TOKEN}" \
                                      -H "Accept: application/vnd.github.v3+json" \
                                      "https://api.github.com/repos/${params.repository}/statuses/${params.sha}" \
                                      -d '{
                                        "state": "pending",
                                        "description": "Jenkins build in progress",
                                        "context": "continuous-integration/jenkins",
                                        "target_url": "${env.BUILD_URL}"
                                      }'
                                """
                            }
                            echo "✅ GitHub status updated to PENDING"
                        } catch (Exception e) {
                            echo "⚠️ Could not update GitHub status: ${e.message}"
                        }
                    }
                }
            }
        }

        stage('Environment Info') {
            steps {
                script {
                    echo "=== Build Information ==="
                    echo "Repository: ${params.repository}"
                    echo "Commit SHA: ${params.sha}"
                    echo "Jenkins Build: ${env.BUILD_NUMBER}"
                }
            }
        }

        stage('Checkout') {
            steps {
                script {
                    // Checkout specific commit
                    if (params.repository && params.sha) {
                        checkout([
                            $class: 'GitSCM',
                            branches: [[name: params.sha]],
                            userRemoteConfigs: [[
                                url: "https://github.com/${params.repository}.git",
                                credentialsId: 'github-credentials'  // Configure this in Jenkins
                            ]]
                        ])
                    } else {
                        // Fallback to SCM polling
                        checkout scm
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    echo "Building application..."
                    // Add your build commands here
                    sh 'echo "Running build..."'
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    echo "Running tests..."
                    def characters = ['Antman', 'Captain America', 'Iron Man']
                    writeFile file: 'marvel-characters.txt', text: characters.join('\n')
                }
            }
        }

        stage('Package') {
            steps {
                sh 'zip marvel-character.zip marvel-characters.txt'
            }
        }

        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'marvel-character.zip', fingerprint: true
            }
        }
    }

    post {
        success {
            script {
                echo "✅ Build succeeded!"

                // Report success to GitHub using API
                if (params.sha && params.repository) {
                    try {
                        withCredentials([string(credentialsId: 'github-status-token', variable: 'GITHUB_TOKEN')]) {
                            sh """
                                curl -f -X POST \
                                  -H "Authorization: token \${GITHUB_TOKEN}" \
                                  -H "Accept: application/vnd.github.v3+json" \
                                  "https://api.github.com/repos/${params.repository}/statuses/${params.sha}" \
                                  -d '{
                                    "state": "success",
                                    "description": "Jenkins build passed",
                                    "context": "continuous-integration/jenkins",
                                    "target_url": "${env.BUILD_URL}"
                                  }'
                            """
                        }
                        echo "✅ GitHub status updated to SUCCESS"
                    } catch (Exception e) {
                        echo "⚠️ Could not update GitHub status: ${e.message}"
                    }
                }
            }
        }

        failure {
            script {
                echo "❌ Build failed!"

                // Report failure to GitHub using API
                if (params.sha && params.repository) {
                    try {
                        withCredentials([string(credentialsId: 'github-status-token', variable: 'GITHUB_TOKEN')]) {
                            sh """
                                curl -f -X POST \
                                  -H "Authorization: token \${GITHUB_TOKEN}" \
                                  -H "Accept: application/vnd.github.v3+json" \
                                  "https://api.github.com/repos/${params.repository}/statuses/${params.sha}" \
                                  -d '{
                                    "state": "failure",
                                    "description": "Jenkins build failed",
                                    "context": "continuous-integration/jenkins",
                                    "target_url": "${env.BUILD_URL}"
                                  }'
                            """
                        }
                        echo "✅ GitHub status updated to FAILURE"
                    } catch (Exception e) {
                        echo "⚠️ Could not update GitHub status: ${e.message}"
                    }
                }
            }
        }

        aborted {
            script {
                echo "⚠️ Build aborted!"

                // Report error to GitHub using API
                if (params.sha && params.repository) {
                    try {
                        withCredentials([string(credentialsId: 'github-status-token', variable: 'GITHUB_TOKEN')]) {
                            sh """
                                curl -f -X POST \
                                  -H "Authorization: token \${GITHUB_TOKEN}" \
                                  -H "Accept: application/vnd.github.v3+json" \
                                  "https://api.github.com/repos/${params.repository}/statuses/${params.sha}" \
                                  -d '{
                                    "state": "error",
                                    "description": "Jenkins build aborted",
                                    "context": "continuous-integration/jenkins",
                                    "target_url": "${env.BUILD_URL}"
                                  }'
                            """
                        }
                        echo "✅ GitHub status updated to ERROR"
                    } catch (Exception e) {
                        echo "⚠️ Could not update GitHub status: ${e.message}"
                    }
                }
            }
        }

        always {
            echo "Build finished: ${currentBuild.result}"
        }
    }
}
