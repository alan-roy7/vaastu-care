@echo off
REM Creates the database + tables and adds sample accounts (only if the database is empty).
REM Use:  scripts\setup-db.bat           (create + sample data)
REM       scripts\setup-db.bat --reset   (DELETE everything and start again)
cd /d "%~dp0\.."
if not exist out ( echo Run scripts\build.bat first. & exit /b 1 )
if "%1"=="--reset" ( java -cp "out;lib\*" com.vaastucare.db.DbSetup --reset ) else ( java -cp "out;lib\*" com.vaastucare.db.DbSetup --seed )
