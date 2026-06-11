@echo off
REM Compile le backend
cd /d "c:\Users\HP\Videos\Projet Application Web\universite-backend"

echo ================================
echo Compilation du Backend...
echo ================================

REM Nettoie et compile
call mvnw.cmd clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Compilation réussie!
    echo Démarrage du backend...
    echo.
    call mvnw.cmd spring-boot:run
) else (
    echo.
    echo ❌ Compilation échouée
    echo Vérifiez les erreurs ci-dessus
    pause
)
