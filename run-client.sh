#!/bin/bash

# Script para ejecutar el CLIENTE de Subastas RMI

# Uso: ./run-client.sh [puerto] [host]

# puerto: Puerto del servidor (por defecto 1099)

# host: Host del servidor (por defecto localhost)

PUERTO=$1
HOST=$2

if [ -z "$PUERTO" ]; then
PUERTO=1099
fi

if [ -z "$HOST" ]; then
HOST=localhost
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
echo "   INICIANDO CLIENTE DE SUBASTAS"
echo "   Conectando a: $HOST:$PUERTO"
echo "   Classpath: $CLASSPATH"
echo "========================================"
echo ""

# Iniciar cliente

java -cp "$CLASSPATH" cliente.ClienteSubastas $PUERTO $HOST
