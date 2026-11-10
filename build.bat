@echo off
echo ========================================================
echo   Building Campus Smart-Parking System
echo ========================================================
if not exist "bin" mkdir "bin"

echo Compiling Java source files...
javac -d bin src\parking\system\facility\*.java src\parking\system\models\*.java src\parking\system\users\*.java src\parking\system\main\*.java tasks\Task03_ParkingFacility\*.java tasks\Task08_FeeDemo\*.java tasks\Task12_Day4Challenge\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    exit /b %ERRORLEVEL%
)

echo Packaging executable standalone JAR...
jar cfe campus-parking.jar parking.system.main.ParkingDemo -C bin .

echo ========================================================
echo   Build Successful! Executable: campus-parking.jar
echo ========================================================
