#!/bin/bash

# Script para ejecutar el SERVIDOR de Subastas RMI

# Uso: ./run-server.sh [puerto]

# puerto: Puerto del registry (por defecto 1099)

PUERTO=$1

if [ -z "$PUERTO" ]; then
PUERTO=1099
fi

# Configurar classpath con librerías externas

LIB_DIR="lib"
CLASSPATH="bin"

if [ -d "$LIB_DIR" ]; then
    for jar in $LIB_DIR/*.jar; do
        if [ -f "$jar" ]; then
            CLASSPATH="$CLASSPATH:$jar"
        fi
    done
fi

echo "========================================"
echo "   INICIANDO SERVIDOR DE SUBASTAS"
echo "   Puerto: $PUERTO"
echo "   Classpath: $CLASSPATH"
echo "========================================"
echo ""

# Iniciar servidor

java -cp "$CLASSPATH" servidor.ServidorSubastas $PUERTO
