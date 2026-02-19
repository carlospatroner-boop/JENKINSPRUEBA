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
                            // El error "UNPROTECTED PRIVATE KEY FILE" ocurre porque el archivo temporal tiene permisos muy abiertos.
                            // Solución: Usar icacls para restringir permisos antes de usar la llave.

                            // 1. Restringir permisos del archivo de llave (solo SYSTEM y Administradores)
                            // Usamos icacls para quitar herencia (/inheritance:r) y dar control total solo al usuario actual (/grant:r "%USERNAME%":F)
                            bat "icacls \"%SSH_KEY_FILE%\" /inheritance:r /grant:r \"%USERNAME%\":F"

                            // 2. Crear directorio
                            bat "ssh -o StrictHostKeyChecking=no -i \"%SSH_KEY_FILE%\" ${VM_USER}@${VM_IP} \"mkdir -p ${REMOTE_DIR}\""

                            // 3. Copiar archivo
                            bat "scp -o StrictHostKeyChecking=no -i \"%SSH_KEY_FILE%\" target/${JAR_NAME} ${VM_USER}@${VM_IP}:${REMOTE_DIR}/${JAR_NAME}"

                            // 4. Ejecutar en remoto
                            bat "ssh -o StrictHostKeyChecking=no -i \"%SSH_KEY_FILE%\" ${VM_USER}@${VM_IP} \"pkill -f ${JAR_NAME} || true && nohup java -jar ${REMOTE_DIR}/${JAR_NAME} > ${REMOTE_DIR}/app.log 2>&1 &\""
                        }
                    }
                }
            }
        }
    }
}