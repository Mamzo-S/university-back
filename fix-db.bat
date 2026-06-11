@echo off
REM Script pour nettoyer la base de données PostgreSQL
REM Exécute le fichier SQL de correction

echo ====================================
echo 🔧 Correction de la Base de Données
echo ====================================
echo.

REM Demander confirmation
set /p confirm="Êtes-vous sûr? (y/n): "
if /i not "%confirm%"=="y" (
    echo Annulé
    exit /b
)

echo.
echo Exécution du script SQL...
echo.

REM Exécuter le script SQL
psql -U postgres -d universite_db -f "src\main\resources\fix-database.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Base de données corrigée!
    echo.
    echo Vous pouvez maintenant redémarrer le backend:
    echo mvnw.cmd clean spring-boot:run
) else (
    echo.
    echo ❌ Erreur lors de l'exécution du script
    echo Vérifiez que PostgreSQL est installé et en cours d'exécution
)

pause
