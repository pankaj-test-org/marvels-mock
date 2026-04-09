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
                    // Report pending status to GitHub using Checks API (supports re-run button)
                    if (params.sha && params.repository) {
                        try {
                            withCredentials([string(credentialsId: 'github-status-token', variable: 'GITHUB_TOKEN')]) {
                                // Create a check run using Checks API
                                sh """
                                    curl -f -X POST \
                                      -H "Authorization: token \${GITHUB_TOKEN}" \
                                      -H "Accept: application/vnd.github.v3+json" \
                                      "https://api.github.com/repos/${params.repository}/check-runs" \
                                      -d '{
                                        "name": "Jenkins CI",
                                        "head_sha": "${params.sha}",
                                        "status": "in_progress",
                                        "started_at": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'",
                                        "details_url": "${env.BUILD_URL}",
                                        "output": {
                                          "title": "Jenkins Build",
                                          "summary": "Jenkins build in progress..."
                                        }
                                      }' > /tmp/check_run_response.json

                                    # Store the check run ID for later updates
                                    cat /tmp/check_run_response.json | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2 > /tmp/check_run_id.txt
                                """
                            }
                            echo "✅ GitHub check run created"
                        } catch (Exception e) {
                            echo "⚠️ Could not create GitHub check run: ${e.message}"
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
                    echo "Workspace: ${env.WORKSPACE}"

                    // Show what commit was actually checked out
                    sh 'git log -1 --oneline'
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

                // Complete the check run with success
                if (params.sha && params.repository) {
                    try {
                        withCredentials([string(credentialsId: 'github-status-token', variable: 'GITHUB_TOKEN')]) {
                            sh """
                                # Get all check runs for this commit and find our Jenkins CI check
                                CHECK_RUN_ID=\$(curl -s \
                                  -H "Authorization: token \${GITHUB_TOKEN}" \
                                  -H "Accept: application/vnd.github.v3+json" \
                                  "https://api.github.com/repos/${params.repository}/commits/${params.sha}/check-runs" | \
                                  grep -A 5 '"name":"Jenkins CI"' | grep '"id":' | head -1 | grep -o '[0-9]*')

                                if [ ! -z "\$CHECK_RUN_ID" ]; then
                                  curl -f -X PATCH \
                                    -H "Authorization: token \${GITHUB_TOKEN}" \
                                    -H "Accept: application/vnd.github.v3+json" \
                                    "https://api.github.com/repos/${params.repository}/check-runs/\$CHECK_RUN_ID" \
                                    -d '{
                                      "status": "completed",
                                      "conclusion": "success",
                                      "completed_at": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'",
                                      "output": {
                                        "title": "Jenkins Build Succeeded",
                                        "summary": "All stages completed successfully"
                                      }
                                    }'
                                fi
                            """
                        }
                        echo "✅ GitHub check run completed with SUCCESS"
                    } catch (Exception e) {
                        echo "⚠️ Could not update GitHub check run: ${e.message}"
                    }
                }
            }
        }

        failure {
            script {
                echo "❌ Build failed!"

                // Complete the check run with failure
                if (params.sha && params.repository) {
                    try {
                        withCredentials([string(credentialsId: 'github-status-token', variable: 'GITHUB_TOKEN')]) {
                            sh """
                                CHECK_RUN_ID=\$(curl -s \
                                  -H "Authorization: token \${GITHUB_TOKEN}" \
                                  -H "Accept: application/vnd.github.v3+json" \
                                  "https://api.github.com/repos/${params.repository}/commits/${params.sha}/check-runs" | \
                                  grep -A 5 '"name":"Jenkins CI"' | grep '"id":' | head -1 | grep -o '[0-9]*')

                                if [ ! -z "\$CHECK_RUN_ID" ]; then
                                  curl -f -X PATCH \
                                    -H "Authorization: token \${GITHUB_TOKEN}" \
                                    -H "Accept: application/vnd.github.v3+json" \
                                    "https://api.github.com/repos/${params.repository}/check-runs/\$CHECK_RUN_ID" \
                                    -d '{
                                      "status": "completed",
                                      "conclusion": "failure",
                                      "completed_at": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'",
                                      "output": {
                                        "title": "Jenkins Build Failed",
                                        "summary": "One or more stages failed"
                                      }
                                    }'
                                fi
                            """
                        }
                        echo "✅ GitHub check run completed with FAILURE"
                    } catch (Exception e) {
                        echo "⚠️ Could not update GitHub check run: ${e.message}"
                    }
                }
            }
        }

        aborted {
            script {
                echo "⚠️ Build aborted!"

                // Complete the check run with cancelled
                if (params.sha && params.repository) {
                    try {
                        withCredentials([string(credentialsId: 'github-status-token', variable: 'GITHUB_TOKEN')]) {
                            sh """
                                CHECK_RUN_ID=\$(curl -s \
                                  -H "Authorization: token \${GITHUB_TOKEN}" \
                                  -H "Accept: application/vnd.github.v3+json" \
                                  "https://api.github.com/repos/${params.repository}/commits/${params.sha}/check-runs" | \
                                  grep -A 5 '"name":"Jenkins CI"' | grep '"id":' | head -1 | grep -o '[0-9]*')

                                if [ ! -z "\$CHECK_RUN_ID" ]; then
                                  curl -f -X PATCH \
                                    -H "Authorization: token \${GITHUB_TOKEN}" \
                                    -H "Accept: application/vnd.github.v3+json" \
                                    "https://api.github.com/repos/${params.repository}/check-runs/\$CHECK_RUN_ID" \
                                    -d '{
                                      "status": "completed",
                                      "conclusion": "cancelled",
                                      "completed_at": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'",
                                      "output": {
                                        "title": "Jenkins Build Cancelled",
                                        "summary": "Build was aborted"
                                      }
                                    }'
                                fi
                            """
                        }
                        echo "✅ GitHub check run completed with CANCELLED"
                    } catch (Exception e) {
                        echo "⚠️ Could not update GitHub check run: ${e.message}"
                    }
                }
            }
        }

        always {
            echo "Build finished: ${currentBuild.result}"
        }
    }
}
