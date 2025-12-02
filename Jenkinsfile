pipeline {
    agent any

    environment {
        // Docker configuration
        DOCKER_COMPOSE_FILE = 'docker-compose.yml'

        // EC2 configuration
        EC2_HOST = '184.169.147.113'
        EC2_USER = 'ubuntu'
        EC2_PROJECT_PATH = '/home/ubuntu/BuzzLink'

        // SSH credentials (configure in Jenkins)
        SSH_CREDENTIALS_ID = 'buzzlink-ec2-ssh-key'

        // Notification settings
        SLACK_CHANNEL = '#deployments' // Optional: configure if using Slack
    }

    options {
        // Keep last 10 builds
        buildDiscarder(logRotator(numToKeepStr: '10'))

        // Timeout for the entire pipeline
        timeout(time: 30, unit: 'MINUTES')

        // Disable concurrent builds
        disableConcurrentBuilds()
    }

    triggers {
        // Poll GitHub for changes every 2 minutes (can be replaced with webhooks)
        pollSCM('H/2 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from GitHub...'
                checkout scm

                script {
                    // Get commit information
                    env.GIT_COMMIT_MSG = sh(
                        script: 'git log -1 --pretty=%B',
                        returnStdout: true
                    ).trim()
                    env.GIT_AUTHOR = sh(
                        script: 'git log -1 --pretty=%an',
                        returnStdout: true
                    ).trim()
                }

                echo "Commit: ${env.GIT_COMMIT_MSG}"
                echo "Author: ${env.GIT_AUTHOR}"
            }
        }

        stage('Build Check') {
            steps {
                echo 'Running build validation...'

                // Optional: Run tests locally before deploying
                script {
                    def backendExists = fileExists('backend/build.gradle')
                    def frontendExists = fileExists('frontend/package.json')

                    if (!backendExists || !frontendExists) {
                        error('Project structure validation failed')
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying application...'

                script {
                    // Since Jenkins is running on the same EC2, run commands locally
                    sh """
                        # Navigate to project directory
                        cd ${env.EC2_PROJECT_PATH}

                        # Stash any local changes before pulling
                        echo "Stashing local changes..."
                        git stash

                        # Pull latest changes
                        echo "Pulling latest code from GitHub..."
                        git pull origin main

                        # Stop existing containers
                        echo "Stopping existing containers..."
                        docker-compose down

                        # Build and start new containers
                        echo "Building and starting new containers..."
                        docker-compose up -d --build

                        # Wait for services to start
                        echo "Waiting for services to start..."
                        sleep 30

                        # Check service health
                        echo "Checking service health..."
                        docker-compose ps

                        # Show logs for verification
                        echo "Recent logs:"
                        docker-compose logs --tail=50
                    """
                }
            }
        }

        stage('Health Check') {
            steps {
                echo 'Performing health checks...'

                script {
                    // Wait a bit for services to fully start
                    sleep(time: 10, unit: 'SECONDS')

                    // Check backend health
                    def backendHealthy = sh(
                        script: "curl -f http://${env.EC2_HOST}:8080/actuator/health || exit 1",
                        returnStatus: true
                    ) == 0

                    // Check frontend health
                    def frontendHealthy = sh(
                        script: "curl -f http://${env.EC2_HOST}:3000 || exit 1",
                        returnStatus: true
                    ) == 0

                    if (!backendHealthy) {
                        error('Backend health check failed!')
                    }

                    if (!frontendHealthy) {
                        error('Frontend health check failed!')
                    }

                    echo 'All health checks passed!'
                }
            }
        }

        stage('Cleanup') {
            steps {
                echo 'Cleaning up old Docker resources...'

                sh """
                    # Remove unused images
                    docker image prune -f
                """
            }
        }
    }

    post {
        success {
            echo '✅ Deployment successful!'

            script {
                def deploymentMsg = """
                    ✅ *Deployment Successful*

                    *Project:* BuzzLink
                    *Branch:* ${env.BRANCH_NAME}
                    *Commit:* ${env.GIT_COMMIT_MSG}
                    *Author:* ${env.GIT_AUTHOR}
                    *Build:* #${env.BUILD_NUMBER}

                    *Services:*
                    - Frontend: http://${env.EC2_HOST}:3000
                    - Backend: http://${env.EC2_HOST}:8080
                    - API Docs: http://${env.EC2_HOST}:8080/actuator/health
                """.stripIndent()

                echo deploymentMsg

                // Optional: Send Slack notification
                // slackSend(channel: env.SLACK_CHANNEL, message: deploymentMsg, color: 'good')
            }
        }

        failure {
            echo '❌ Deployment failed!'

            script {
                def failureMsg = """
                    ❌ *Deployment Failed*

                    *Project:* BuzzLink
                    *Branch:* ${env.BRANCH_NAME}
                    *Build:* #${env.BUILD_NUMBER}
                    *Author:* ${env.GIT_AUTHOR}

                    Please check Jenkins logs for details.
                """.stripIndent()

                echo failureMsg

                // Optional: Send Slack notification
                // slackSend(channel: env.SLACK_CHANNEL, message: failureMsg, color: 'danger')
            }
        }

        always {
            echo 'Pipeline execution completed.'

            // Cleanup workspace if needed
            // cleanWs()
        }
    }
}
