// Jenkinsfile for testing CloudBees GitHub Reporting ReRunCause

pipeline {
    agent any

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
                    def isRerun = false
                    causes.each { cause ->
                        echo "Cause: ${cause}"
                        if (cause.toString().contains('ReRunCause') || cause.toString().contains('UserIdCause')) {
                            isRerun = true
                        }
                    }

                    if (isRerun) {
                        echo "⚠️ RERUN DETECTED - Skipping GitHub checks requirement"
                        env.SKIP_GITHUB_CHECKS = 'true'
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
                    def shouldFail = env.JENKINS_FAIL_BUILD == 'true'
                    if (shouldFail) {
                        echo "⚠️ JENKINS_FAIL_BUILD=true - failing intentionally"
                        error("Build failed intentionally (set JENKINS_FAIL_BUILD=false to pass)")
                    } else {
                        echo "✅ Tests passed (JENKINS_FAIL_BUILD=${env.JENKINS_FAIL_BUILD ?: 'not set'})"
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
