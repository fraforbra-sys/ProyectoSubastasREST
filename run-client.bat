@echo off
REM Script para ejecutar el CLIENTE de Subastas RMI
REM Uso: run-client.bat [puerto] [host]
REM   puerto: Puerto del servidor (por defecto 1099)
REM   host: Host del servidor (por defecto localhost)

setlocal enabledelayedexpansion

set PUERTO=%1
set HOST=%2
if "%PUERTO%"=="" set PUERTO=1099
if "%HOST%"=="" set HOST=localhost

REM Configurar classpath con librerías externas
set CLASSPATH=bin
if exist "lib" (
    for %%i in (lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%i
)

echo ========================================
echo   INICIANDO CLIENTE DE SUBASTAS
echo   Conectando a: %HOST%:%PUERTO%
echo   Classpath: %CLASSPATH%
echo ========================================
echo.

REM Iniciar el cliente
java -cp %CLASSPATH% cliente.ClienteSubastas %PUERTO% %HOST%
