// Jenkinsfile for testing CloudBees GitHub Reporting ReRunCause

pipeline {
    agent any

    parameters {
        booleanParam(name: 'FAIL_BUILD', defaultValue: false, description: 'Set to true to intentionally fail the build')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }

    stages {
        stage('Build Info') {
            steps {
                script {
                    echo "=== Build Information ==="
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Build: #${env.BUILD_NUMBER}"

                    // Show build cause - look for ReRunCause
                    def causes = currentBuild.getBuildCauses()
                    causes.each { cause ->
                        echo "Cause: ${cause}"
                    }
                }
            }
        }

        stage('Build') {
            steps {
                echo "Building..."
                sh 'echo "Build stage completed"'
            }
        }

        stage('Test') {
            steps {
                echo "Running tests..."
                script {
                    if (params.FAIL_BUILD) {
                        error("Build failed intentionally (FAIL_BUILD=true)")
                    } else {
                        echo "Tests passed"
                    }
                }
            }
        }

        stage('Package') {
            steps {
                echo "Packaging..."
                sh 'echo "Package stage completed"'
            }
        }
    }

    post {
        always {
            echo "Build finished: ${currentBuild.result}"
        }
    }
}
