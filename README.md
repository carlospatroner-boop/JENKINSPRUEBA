# Microservicio de Prueba para Jenkins y Azure

Este proyecto es un microservicio básico construido con **Java 17** y **Spring Boot 3**. Su propósito es demostrar un flujo de CI/CD utilizando **Jenkins** para desplegar automáticamente en una **Máquina Virtual de Azure**.

## Requisitos Previos

1.  **Java JDK 17** instalado localmente.
2.  **Maven** instalado (o usar el wrapper incluido).
3.  Una cuenta de **Azure** activa.
4.  Un servidor **Jenkins** configurado y accesible.

## Estructura del Proyecto

-   `src/main/java`: Código fuente de la aplicación.
-   `Jenkinsfile`: Pipeline de Jenkins para construir, probar y desplegar.
-   `pom.xml`: Configuración de Maven y dependencias.

## Configuración en Azure

1.  Crea una **Máquina Virtual (VM)** en Azure (Ubuntu Server recomendado).
2.  Asegúrate de abrir el puerto **8080** (o el que uses) en el **Network Security Group (NSG)** de la VM para permitir el tráfico entrante.
3.  Conéctate a la VM por SSH e instala Java:
    ```bash
    sudo apt update
    sudo apt install openjdk-17-jdk -y
    java -version
    ```

## Configuración en Jenkins

1.  **Instalar Plugins**: Asegúrate de tener instalados los plugins "Pipeline", "Git", "SSH Agent" y "Maven Integration".
2.  **Configurar Herramientas Globales**:
    -   Ve a *Manage Jenkins* -> *Global Tool Configuration*.
    -   Añade una instalación de JDK llamada `JDK 17`.
    -   Añade una instalación de Maven llamada `Maven 3.9.6`.
3.  **Configurar Credenciales**:
    -   Ve a *Manage Jenkins* -> *Credentials*.
    -   Añade una nueva credencial de tipo **SSH Username with private key**.
    -   ID: `azure-vm-ssh-key` (o el que uses en el Jenkinsfile).
    -   Username: El usuario de tu VM (ej. `azureuser`).
    -   Private Key: Pega el contenido de tu clave privada `.pem` o `id_rsa`.

## Ejecución del Pipeline

1.  Crea un nuevo **Pipeline** en Jenkins.
2.  En la sección *Pipeline*, selecciona "Pipeline script from SCM".
3.  Configura tu repositorio Git (donde subas este código).
4.  Asegúrate de que el *Script Path* sea `Jenkinsfile`.
5.  Guarda y ejecuta ("Build Now").

## Verificar el Despliegue

Una vez que el pipeline termine exitosamente, abre tu navegador y visita:

`http://<TU-IP-PUBLICA-AZURE>:8080/`

Deberías ver un mensaje JSON de bienvenida.