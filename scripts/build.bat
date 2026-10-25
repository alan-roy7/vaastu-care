@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0\.."
if not exist "lib\mysql-connector-j-8.4.0.jar" (
  echo MySQL driver missing: lib\mysql-connector-j-8.4.0.jar - see README step 4.
  exit /b 1
)
if exist out rmdir /s /q out
mkdir out
if exist sources.txt del sources.txt
for /r "src\main\java" %%f in (*.java) do (
  set "p=%%f"
  set "p=!p:\=/!"
  echo "!p!">>sources.txt
)
javac -encoding UTF-8 -cp "lib\*" -d out @sources.txt
if errorlevel 1 ( echo Compile FAILED. & exit /b 1 )
echo Build OK.
