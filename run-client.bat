@echo off
REM Script para ejecutar el CLIENTE de Subastas RMI
REM Uso: run-client.bat [puerto] [host]
REM   puerto: Puerto del servidor (por defecto 1099)
REM   host: Host del servidor (por defecto localhost)

set PUERTO=%1
set HOST=%2
if "%PUERTO%"=="" set PUERTO=1099
if "%HOST%"=="" set HOST=localhost

echo ========================================
echo   INICIANDO CLIENTE DE SUBASTAS
echo   Conectando a: %HOST%:%PUERTO%
echo ========================================
echo.

REM Iniciar el cliente
java -cp bin cliente.ClienteSubastas %PUERTO% %HOST%
