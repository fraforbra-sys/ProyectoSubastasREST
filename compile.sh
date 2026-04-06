#!/bin/bash

echo "========================================"
echo "   COMPILACIÓN - Sistema de Subastas RMI"
echo "========================================"

# Establecer encoding UTF-8

export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

# Crear directorio de salida si no existe

if [ ! -d "bin" ]; then
mkdir bin
fi

# Compilar clases comunes

echo "[1/4] Compilando clases de datos (comun/)..."
javac -d bin comun/*.java
if [ $? -ne 0 ]; then
echo "ERROR: Fallo en la compilación de comun/"
exit 1
fi

# Compilar servidor

echo "[2/4] Compilando clases del servidor (servidor/)..."
javac -cp bin -d bin servidor/*.java
if [ $? -ne 0 ]; then
echo "ERROR: Fallo en la compilación del servidor"
exit 1
fi

# Compilar cliente

echo "[3/4] Compilando clases del cliente (cliente/)..."
javac -cp bin -d bin cliente/*.java
if [ $? -ne 0 ]; then
echo "ERROR: Fallo en la compilación del cliente"
exit 1
fi

# rmic (opcional)

echo "[4/4] Generando stubs con rmic (opcional en Java 5+)..."

# rmic -d bin servidor.SubastaImpl servidor.GestorSubastasImpl

echo ""
echo "========================================"
echo "   COMPILACIÓN COMPLETADA CON ÉXITO"
echo "========================================"
echo ""
echo "Estructura generada:"
echo "  bin/"
echo "    comun/    - Clases de datos e interfaces"
echo "    servidor/ - Implementaciones del servidor"
echo "    cliente/  - Implementaciones del cliente"
echo ""
echo "Para ejecutar:"
echo "  Servidor: ./run-server.sh [puerto]"
echo "  Cliente: ./run-client.sh [puerto] [host]"
echo ""
