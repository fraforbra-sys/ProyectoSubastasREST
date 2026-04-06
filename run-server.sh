#!/bin/bash

# Script para ejecutar el SERVIDOR de Subastas RMI

# Uso: ./run-server.sh [puerto]

# puerto: Puerto del registry (por defecto 1099)

PUERTO=$1

if [ -z "$PUERTO" ]; then
PUERTO=1099
fi

echo "========================================"
echo "   INICIANDO SERVIDOR DE SUBASTAS"
echo "   Puerto: $PUERTO"
echo "========================================"
echo ""

# Iniciar servidor

java -cp bin servidor.ServidorSubastas $PUERTO
