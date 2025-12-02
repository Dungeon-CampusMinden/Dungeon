@echo off
REM Default to 'y' so simply pressing Enter selects Yes
set "answer=y"
set /p "answer=Web Version [Y/n]? "

REM Check for explicit "no" inputs (case insensitive)
if /I "%answer%"=="n" goto :run_java
if /I "%answer%"=="no" goto :run_java

REM --- Web Version (Default) ---
echo Starting Web Version...

REM Start Gradle in background
start "Gradle Server" cmd /c "gradlew runBlockly -Pweb=true"

echo Starting Blockly frontend...
cd blockly\frontend || ( echo Directory not found & pause & exit /b )

REM Start NPM in background
start "NPM Server" cmd /c "npm run dev"

echo Waiting 5 seconds before opening browser...
timeout /t 5 /nobreak >nul

REM Open browser
start http://localhost:5173/

echo.
echo Servers are running in separate windows.
echo Press any key to stop java/node processes.
pause >nul
goto :cleanup

:run_java
echo Starting Java Dungeon...
gradlew runBlockly
goto :eof

:cleanup
echo Stopping servers...
taskkill /IM java.exe /F >nul 2>&1
taskkill /IM node.exe /F >nul 2>&1
echo All servers stopped.
exit /b
