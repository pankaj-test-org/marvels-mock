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
        string(name: 'repository', defaultValue: '', description: 'GitHub repository')
        string(name: 'ref', defaultValue: '', description: 'Git ref')
        string(name: 'sha', defaultValue: '', description: 'Commit SHA')
        string(name: 'branch', defaultValue: 'main', description: 'Branch name')
        string(name: 'author', defaultValue: '', description: 'Commit author')
        string(name: 'commit_message', defaultValue: '', description: 'Commit message')
        string(name: 'run_id', defaultValue: '', description: 'GitHub Actions run ID')
    }

    stages {
        stage('Set GitHub Status - Pending') {
            steps {
                script {
                    // Report pending status to GitHub
                    // Requires GitHub credentials configured in Jenkins
                    if (params.sha && params.repository) {
                        githubNotify(
                            account: params.repository.split('/')[0],
                            repo: params.repository.split('/')[1],
                            sha: params.sha,
                            status: 'PENDING',
                            description: 'Jenkins build in progress',
                            context: 'continuous-integration/jenkins',
                            credentialsId: 'github-status-token'
                        )
                    }
                }
            }
        }

        stage('Environment Info') {
            steps {
                script {
                    echo "=== Build Information ==="
                    echo "Repository: ${params.repository}"
                    echo "Branch: ${params.branch}"
                    echo "Commit: ${params.sha}"
                    echo "Author: ${params.author}"
                    echo "Message: ${params.commit_message}"
                    echo "GitHub Run ID: ${params.run_id}"
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

                // Report success to GitHub
                if (params.sha && params.repository) {
                    githubNotify(
                        account: params.repository.split('/')[0],
                        repo: params.repository.split('/')[1],
                        sha: params.sha,
                        status: 'SUCCESS',
                        description: 'Jenkins build passed',
                        context: 'continuous-integration/jenkins',
                        credentialsId: 'github-status-token'
                    )
                }
            }
        }

        failure {
            script {
                echo "❌ Build failed!"

                // Report failure to GitHub
                if (params.sha && params.repository) {
                    githubNotify(
                        account: params.repository.split('/')[0],
                        repo: params.repository.split('/')[1],
                        sha: params.sha,
                        status: 'FAILURE',
                        description: 'Jenkins build failed',
                        context: 'continuous-integration/jenkins',
                        credentialsId: 'github-status-token'
                    )
                }
            }
        }

        aborted {
            script {
                echo "⚠️ Build aborted!"

                // Report error to GitHub
                if (params.sha && params.repository) {
                    githubNotify(
                        account: params.repository.split('/')[0],
                        repo: params.repository.split('/')[1],
                        sha: params.sha,
                        status: 'ERROR',
                        description: 'Jenkins build aborted',
                        context: 'continuous-integration/jenkins',
                        credentialsId: 'github-status-token'
                    )
                }
            }
        }

        always {
            echo "Build finished: ${currentBuild.result}"
        }
    }
}
