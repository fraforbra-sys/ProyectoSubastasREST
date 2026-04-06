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

echo "========================================"
echo "   INICIANDO CLIENTE DE SUBASTAS"
echo "   Conectando a: $HOST:$PUERTO"
echo "========================================"
echo ""

# Iniciar cliente

java -cp bin cliente.ClienteSubastas $PUERTO $HOST
