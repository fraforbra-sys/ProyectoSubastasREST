@echo off
REM Script de compilación para el Proyecto de Subastas RMI
REM Uso: compile.bat

echo ========================================
echo   COMPILACION - Sistema de Subastas RMI
echo ========================================

REM Establecer encoding UTF-8
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8

REM Crear directorio de salida si no existe
if not exist "bin" mkdir bin

REM Compilar todas las clases
echo [1/4] Compilando clases de datos (comun/)...
javac -d bin comun/*.java
if errorlevel 1 (
    echo ERROR: Fallo en la compilacion de comun/
    exit /b 1
)

echo [2/4] Compilando clases del servidor (servidor/)...
javac -cp bin -d bin servidor/*.java
if errorlevel 1 (
    echo ERROR: Fallo en la compilacion del servidor
    exit /b 1
)

echo [3/4] Compilando clases del cliente (cliente/)...
javac -cp bin -d bin cliente/*.java
if errorlevel 1 (
    echo ERROR: Fallo en la compilacion del cliente
    exit /b 1
)

echo [4/4] Generando stubs con rmic (opcional en Java 5+)...
REM En Java 5+ no es necesario rmic, pero lo dejamos por compatibilidad
REM rmic -d bin servidor.SubastaImpl servidor.GestorSubastasImpl

echo.
echo ========================================
echo   COMPILACION COMPLETADA CON EXITO
echo ========================================
echo.
echo Estructura generada:
echo   bin/
echo     comun/    - Clases de datos e interfaces
echo     servidor/ - Implementaciones del servidor
echo     cliente/  - Implementaciones del cliente
echo.
echo Para ejecutar:
echo   Servidor: run-server.bat [puerto]
echo   Cliente: run-client.bat [puerto] [host]
echo.
