pipeline {
    agent any
    stages {
        stage('Echo Characters') {
            steps {
                script {
                    def characters = ['Antman', 'Captain America', 'Iron Man']
                    writeFile file: 'marvel-characters.txt', text: characters.join('\n')
                }
            }
        }
        stage('Zip File') {
            steps {
                sh 'zip marvel-character.zip marvel-characters.txt'
            }
        }
        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: 'marvel-character.zip', fingerprint: true
            }
        }
    }
}

