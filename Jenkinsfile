pipeline {
    agent any

    tools {
        // Asegúrate de que 'Maven 3.9.6' y 'JDK 17' coincidan con los nombres en "Global Tool Configuration" de Jenkins
        maven 'Maven 3.9.6'
        jdk 'JDK 17'
    }

    environment {
        // --- CONFIGURACIÓN DE TU ENTORNO AZURE ---
        // Usuario de la máquina virtual (ej. azureuser)
        VM_USER = 'azureuser'

        // IP Pública de tu máquina virtual en Azure
        VM_IP = '20.220.170.47'

        // ID de la credencial SSH guardada en Jenkins (Dashboard -> Manage Jenkins -> Credentials)
        // Debe ser una credencial de tipo "SSH Username with private key"
        SSH_CREDENTIALS_ID = 'azure-vm-ssh-key'

        // Nombre del archivo JAR generado
        JAR_NAME = 'jenkins-azure-demo-0.0.1-SNAPSHOT.jar'

        // Directorio en la VM donde se alojará la app
        REMOTE_DIR = '/home/azureuser/app'
    }

    stages {
        stage('Build') {
            steps {
                echo 'Compilando el proyecto...'
                // Ejecuta Maven para limpiar y empaquetar el proyecto
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo 'Ejecutando pruebas unitarias...'
                sh 'mvn test'
            }
        }

        stage('Deploy to Azure VM') {
            steps {
                // Usamos el plugin SSH Agent para cargar la llave privada en memoria
                sshagent(credentials: ["${SSH_CREDENTIALS_ID}"]) {
                    echo "Conectando a ${VM_IP}..."

                    // 1. Crear directorio remoto si no existe
                    sh "ssh -o StrictHostKeyChecking=no ${VM_USER}@${VM_IP} 'mkdir -p ${REMOTE_DIR}'"

                    // 2. Copiar el archivo JAR compilado al servidor remoto
                    sh "scp -o StrictHostKeyChecking=no target/${JAR_NAME} ${VM_USER}@${VM_IP}:${REMOTE_DIR}/${JAR_NAME}"

                    // 3. Detener la aplicación anterior (si existe) y arrancar la nueva
                    // Se busca el proceso por el nombre del JAR y se mata.
                    // Luego se inicia con 'nohup' para que siga corriendo en segundo plano.
                    sh """
                        ssh -o StrictHostKeyChecking=no ${VM_USER}@${VM_IP} '
                            echo "Buscando proceso anterior..."
                            PID=\$(pgrep -f "${JAR_NAME}" || true)

                            if [ -n "\$PID" ]; then
                                echo "Deteniendo proceso existente con PID: \$PID"
                                kill -9 \$PID
                            else
                                echo "No se encontró proceso anterior."
                            fi

                            echo "Iniciando nueva versión de la aplicación..."
                            # Ejecutar en segundo plano y redirigir logs
                            nohup java -jar ${REMOTE_DIR}/${JAR_NAME} > ${REMOTE_DIR}/app.log 2>&1 &

                            echo "Despliegue completado."
                        '
                    """
                }
            }
        }
    }
}