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
                            // El usuario actual es SYSTEM (EAZY$), lo que complica icacls con %USERNAME%.
                            // Solución: Copiar la llave a un archivo local 'key.pem' y asignar permisos explícitos a SYSTEM y Administradores.

                            // 1. Copiar la llave a un archivo temporal local
                            bat 'copy /Y "%SSH_KEY_FILE%" key.pem'

                            // 2. Restringir permisos usando SIDs universales (S-1-5-18 es SYSTEM, S-1-5-32-544 es Admins)
                            // Primero quitamos herencia y borramos permisos existentes (/inheritance:r)
                            // Luego damos control total a SYSTEM y Admins (/grant *S-1-5-18:F ...)
                            bat 'icacls key.pem /inheritance:r /grant *S-1-5-18:F *S-1-5-32-544:F'

                            // 3. Usar la nueva llave "key.pem"
                            bat "ssh -o StrictHostKeyChecking=no -i key.pem ${VM_USER}@${VM_IP} \"mkdir -p ${REMOTE_DIR}\""
                            bat "scp -o StrictHostKeyChecking=no -i key.pem target/${JAR_NAME} ${VM_USER}@${VM_IP}:${REMOTE_DIR}/${JAR_NAME}"
                            bat "ssh -o StrictHostKeyChecking=no -i key.pem ${VM_USER}@${VM_IP} \"pkill -f ${JAR_NAME} || true && nohup java -jar ${REMOTE_DIR}/${JAR_NAME} > ${REMOTE_DIR}/app.log 2>&1 &\""

                            // 4. Borrar llave temporal
                            bat 'del key.pem'
                        }
                    }
                }
            }
        }
    }
}