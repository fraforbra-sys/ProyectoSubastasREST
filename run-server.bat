@echo off
REM Script para ejecutar el SERVIDOR de Subastas RMI
REM Uso: run-server.bat [puerto]
REM   puerto: Puerto del registry (por defecto 1099)

setlocal enabledelayedexpansion

set PUERTO=%1
if "%PUERTO%"=="" set PUERTO=1099

REM Configurar classpath con librerías externas
set CLASSPATH=bin
if exist "lib" (
    for %%i in (lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%i
)

echo ========================================
echo   INICIANDO SERVIDOR DE SUBASTAS
echo   Puerto: %PUERTO%
echo   Classpath: %CLASSPATH%
echo ========================================
echo.

REM Iniciar el servidor
java -cp %CLASSPATH% servidor.ServidorSubastas %PUERTO%
