#!/bin/bash

# 1. Limpieza de residuos
# Buscamos y eliminamos todos los ficheros .class generados anteriormente
# para asegurar una compilación limpia desde cero.
echo "Limpiando ficheros .class antiguos..."
find . -name "*.class" -delete

echo "Inciamos posgresql"
sudo systemctl start postgresql

# 2. Compilación de la capa de comunicación (Interfaces y Clases Serializables)
# Compilamos primero el paquete 'comun' porque contiene las interfaces Remotas
# y los objetos que se pasan por valor (Serializable), necesarios para los otros módulos.
echo "Compilando paquete comun..."
javac comun/*.java

# 3. Compilación del Servidor
# Usamos '-cp' (classpath) para incluir el directorio actual (.) y la carpeta 'comun'.
# Esto permite que el servidor encuentre las interfaces e implementaciones necesarias.
echo "Compilando servidor..."
javac -cp .:comun servidor/*.java

# 4. Compilación del Cliente
# Al igual que en el servidor, el cliente necesita conocer las interfaces del paquete 'comun'
# para poder realizar el casting del objeto obtenido mediante Naming.lookup.
echo "Compilando cliente..."
javac -cp .:comun cliente/*.java

echo "Proceso finalizado con éxito."
