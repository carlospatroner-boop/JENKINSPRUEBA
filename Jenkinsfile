pipeline {
    agent any

    tools {
        maven 'Maven 3.9.6'
        jdk 'JDK 17'
    }

    environment {
        VM_USER = 'azureuser'
        VM_IP = '20.220.170.47'
        SSH_CREDENTIALS_ID = 'azure-vm-ssh-key'
        JAR_NAME = 'jenkins-azure-demo-0.0.1-SNAPSHOT.jar'
        REMOTE_DIR = '/home/azureuser/app'
    }

    stages {
        stage('Build') {
            steps {
                echo 'Compilando el proyecto...'
                script {
                    if (isUnix()) {
                        sh 'mvn clean package -DskipTests'
                    } else {
                        bat 'mvn clean package -DskipTests'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Ejecutando pruebas unitarias...'
                script {
                    if (isUnix()) {
                        sh 'mvn test'
                    } else {
                        bat 'mvn test'
                    }
                }
            }
        }

        stage('Deploy to Azure VM') {
            steps {
                // Usamos withCredentials en lugar de sshagent para evitar el error de parseo en Windows
                withCredentials([sshUserPrivateKey(credentialsId: "${SSH_CREDENTIALS_ID}", keyFileVariable: 'SSH_KEY_FILE', usernameVariable: 'SSH_USER')]) {
                    echo "Conectando a ${VM_IP}..."

                    script {
                        if (isUnix()) {
                            sh 'chmod 600 $SSH_KEY_FILE'
                            sh "ssh -o StrictHostKeyChecking=no -i \$SSH_KEY_FILE ${VM_USER}@${VM_IP} 'mkdir -p ${REMOTE_DIR}'"
                            sh "scp -o StrictHostKeyChecking=no -i \$SSH_KEY_FILE target/${JAR_NAME} ${VM_USER}@${VM_IP}:${REMOTE_DIR}/${JAR_NAME}"
                            sh """
                                ssh -o StrictHostKeyChecking=no -i \$SSH_KEY_FILE ${VM_USER}@${VM_IP} '
                                    PID=\$(pgrep -f "${JAR_NAME}" || true)
                                    if [ -n "\$PID" ]; then kill -9 \$PID; fi
                                    nohup java -jar ${REMOTE_DIR}/${JAR_NAME} > ${REMOTE_DIR}/app.log 2>&1 &
                                '
                            """
                        } else {
                            // Entorno Windows:
                            // Usamos la llave temporal directamente con ssh -i

                            // 1. Crear directorio
                            bat "ssh -o StrictHostKeyChecking=no -i \"%SSH_KEY_FILE%\" ${VM_USER}@${VM_IP} \"mkdir -p ${REMOTE_DIR}\""

                            // 2. Copiar archivo
                            bat "scp -o StrictHostKeyChecking=no -i \"%SSH_KEY_FILE%\" target/${JAR_NAME} ${VM_USER}@${VM_IP}:${REMOTE_DIR}/${JAR_NAME}"

                            // 3. Ejecutar en remoto
                            bat "ssh -o StrictHostKeyChecking=no -i \"%SSH_KEY_FILE%\" ${VM_USER}@${VM_IP} \"pkill -f ${JAR_NAME} || true && nohup java -jar ${REMOTE_DIR}/${JAR_NAME} > ${REMOTE_DIR}/app.log 2>&1 &\""
                        }
                    }
                }
            }
        }
    }
}