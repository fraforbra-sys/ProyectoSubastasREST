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

# Configurar classpath con librerías externas (SQLite y BCrypt)

LIB_DIR="lib"
CLASSPATH="bin"

if [ -d "$LIB_DIR" ]; then
    for jar in $LIB_DIR/*.jar; do
        if [ -f "$jar" ]; then
            CLASSPATH="$CLASSPATH:$jar"
        fi
    done
fi

echo "Classpath: $CLASSPATH"

# Compilar clases comunes

echo "[1/5] Compilando clases de datos (comun/)..."
javac -d bin comun/*.java
if [ $? -ne 0 ]; then
echo "ERROR: Fallo en la compilación de comun/"
exit 1
fi

# Compilar servidor (incluye dao y servicio)

echo "[2/5] Compilando clases del servidor (servidor/ y subdirectorios)..."
javac -cp "$CLASSPATH" -d bin servidor/**/*.java servidor/*.java 2>/dev/null || \
javac -cp "$CLASSPATH" -d bin servidor/dao/*.java servidor/servicio/*.java servidor/*.java
if [ $? -ne 0 ]; then
echo "ERROR: Fallo en la compilación del servidor"
exit 1
fi

# Compilar cliente

echo "[3/5] Compilando clases del cliente (cliente/)..."
javac -cp "$CLASSPATH" -d bin cliente/*.java
if [ $? -ne 0 ]; then
echo "ERROR: Fallo en la compilación del cliente"
exit 1
fi

# rmic (opcional)

echo "[4/5] Generando stubs con rmic (opcional en Java 5+)..."

# rmic -d bin servidor.SubastaImpl servidor.GestorSubastasImpl

# Copiar resources

echo "[5/5] Copiando recursos..."
if [ -d "db" ]; then
    cp -r db bin/ 2>/dev/null || true
fi

echo ""
echo "========================================"
echo "   COMPILACIÓN COMPLETADA CON ÉXITO"
echo "========================================"
echo ""
echo "Estructura generada:"
echo "  bin/"
echo "    comun/     - Clases de datos e interfaces"
echo "    servidor/  - Implementaciones del servidor, dao, servicio"
echo "    cliente/   - Implementaciones del cliente"
echo "    db/        - Base de datos SQLite"
echo ""
echo "Dependencias requeridas en lib/:"
echo "  - sqlite-jdbc.jar"
echo "  - jbcrypt.jar"
echo ""
echo "Para ejecutar:"
echo "  Servidor: ./run-server.sh [puerto]"
echo "  Cliente: ./run-client.sh [puerto] [host]"
echo ""
