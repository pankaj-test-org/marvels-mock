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
        string(name: 'triggered_by', defaultValue: 'unknown', description: 'GitHub user who triggered the build')
        string(name: 'trigger_cause', defaultValue: 'Generic Cause', description: 'Build trigger description')
        string(name: 'event', defaultValue: 'webhook', description: 'GitHub event type')
    }

    stages {
        stage('Environment Info') {
            steps {
                script {
                    // Set build description to show who triggered it
                    currentBuild.description = "${params.trigger_cause}"
                    currentBuild.displayName = "#${env.BUILD_NUMBER} - ${params.triggered_by}"

                    echo "=== Build Information ==="
                    echo "Triggered by: ${params.triggered_by}"
                    echo "Trigger cause: ${params.trigger_cause}"
                    echo "Event: ${params.event}"
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
        always {
            echo "Build finished: ${currentBuild.result}"
        }
    }
}
