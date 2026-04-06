@echo off
REM Script para ejecutar el SERVIDOR de Subastas RMI
REM Uso: run-server.bat [puerto]
REM   puerto: Puerto del registry (por defecto 1099)

set PUERTO=%1
if "%PUERTO%"=="" set PUERTO=1099

echo ========================================
echo   INICIANDO SERVIDOR DE SUBASTAS
echo   Puerto: %PUERTO%
echo ========================================
echo.

REM Iniciar el servidor
java -cp bin servidor.ServidorSubastas %PUERTO%
