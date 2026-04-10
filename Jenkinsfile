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
                    echo "Repository: ${params.repository}"
                    echo "Branch: ${params.branch}"
                    echo "Commit: ${params.sha}"
                    echo "Author: ${params.author}"
                    echo "Message: ${params.commit_message}"
                    echo "GitHub Run ID: ${params.run_id}"
                    echo "Jenkins Build: ${env.BUILD_NUMBER}"

                    // Show build cause (will be ReRunCause for re-runs)
                    def causes = currentBuild.getBuildCauses()
                    causes.each { cause ->
                        echo "Cause: ${cause}"
                    }
                }
            }
        }

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
                    echo "Running tests..."
                    def characters = ['Antman', 'Captain America', 'Iron Man']
                    writeFile file: 'marvel-characters.txt', text: characters.join('\n')

                    // Optional failure for testing Re-run button
                    // Set JENKINS_FAIL_BUILD=true in job configuration to enable
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
