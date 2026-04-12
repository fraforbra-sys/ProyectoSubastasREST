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

REM Configurar classpath con librerías externas
set CLASSPATH=bin
if exist "lib" (
    for %%i in (lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%i
)

echo Classpath: %CLASSPATH%

REM Compilar clases comunes
echo [1/5] Compilando clases de datos (comun/)...
javac -d bin comun/*.java
if errorlevel 1 (
    echo ERROR: Fallo en la compilacion de comun/
    exit /b 1
)

REM Compilar servidor (incluye dao y servicio)
echo [2/5] Compilando clases del servidor (servidor/ y subdirectorios)...
javac -cp %CLASSPATH% -d bin servidor\dao\*.java servidor\servicio\*.java servidor\*.java
if errorlevel 1 (
    echo ERROR: Fallo en la compilacion del servidor
    exit /b 1
)

REM Compilar cliente
echo [3/5] Compilando clases del cliente (cliente/)...
javac -cp %CLASSPATH% -d bin cliente/*.java
if errorlevel 1 (
    echo ERROR: Fallo en la compilacion del cliente
    exit /b 1
)

echo [4/5] Generando stubs con rmic (opcional en Java 5+)...
REM En Java 5+ no es necesario rmic
REM rmic -d bin servidor.SubastaImpl servidor.GestorSubastasImpl

echo [5/5] Copiando recursos...
if exist "db" xcopy /E /I /Y db bin\db 2>nul

echo.
echo ========================================
echo   COMPILACION COMPLETADA CON EXITO
echo ========================================
echo.
echo Estructura generada:
echo   bin/
echo     comun/     - Clases de datos e interfaces
echo     servidor/  - Implementaciones del servidor, dao, servicio
echo     cliente/   - Implementaciones del cliente
echo     db/        - Base de datos SQLite
echo.
echo Dependencias requeridas en lib/:
echo   - sqlite-jdbc.jar
echo   - jbcrypt.jar
echo.
echo Para ejecutar:
echo   Servidor: run-server.bat [puerto]
echo   Cliente: run-client.bat [puerto] [host]
echo.
