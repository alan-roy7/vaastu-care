@echo off
REM Starts Vaastu Care (MySQL must be running). Add --mock to run with temporary in-memory demo data.
cd /d "%~dp0\.."
if not exist out ( echo Run scripts\build.bat first. & exit /b 1 )
java -cp "out;lib\*" com.vaastucare.Main %*
