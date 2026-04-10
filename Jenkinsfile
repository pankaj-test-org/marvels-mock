// Jenkinsfile for Multibranch Pipeline with CloudBees SCM Reporting
// CloudBees SCM Reporting plugin handles GitHub check runs and re-run functionality

pipeline {
    agent any

    options {
        // Keep builds for 30 days
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }

    stages {
        stage('Environment Info') {
            steps {
                script {
                    echo "=== Build Information ==="
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Build Number: ${env.BUILD_NUMBER}"
                    echo "Workspace: ${env.WORKSPACE}"

                    // Show what commit was checked out
                    sh 'git log -1 --oneline'

                    // Show build cause (will be RerunCause for re-runs)
                    def causes = currentBuild.getBuildCauses()
                    causes.each { cause ->
                        echo "Cause: ${cause}"
                    }
                }
            }
        }

        stage('Build') {
            steps {
                echo "Building application..."
                sh 'echo "Running build..."'
            }
        }

        stage('Test') {
            steps {
                echo "Running tests..."
                script {
                    def characters = ['Antman', 'Captain America', 'Iron Man']
                    writeFile file: 'marvel-characters.txt', text: characters.join('\n')

                    // Intentionally fail to test re-run functionality
                    error("Simulated test failure to trigger re-run button in GitHub")
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
