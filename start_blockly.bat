@echo off
REM Function to cleanup background processes
REM Note: Batch doesn't have a direct trap, so user must Ctrl+C manually
:cleanup
echo Stopping servers...
if defined gradle_pid taskkill /PID %gradle_pid% /F >nul 2>&1
if defined npm_pid taskkill /PID %npm_pid% /F >nul 2>&1
echo All servers stopped.
exit /b

REM Ask user for web version
set /p answer=Web Version (yes/no)?

if /I "%answer%"=="yes" (
    echo Starting Web Version...
    REM Start Gradle in background
    start "" cmd /c "gradlew runBlockly -Pweb=true"
    REM Capture Gradle PID is tricky in batch; skipping for simplicity

    echo Starting Blockly frontend...
    cd blockly\frontend || exit /b
    start "" cmd /c "npm run dev"

    echo Waiting 5 seconds before opening browser...
    timeout /t 5 /nobreak >nul

    REM Open browser
    start http://localhost:5173/

    echo Press CTRL+C to stop everything
    REM Wait indefinitely
    pause >nul
) else (
    echo Starting Java Dungeon...
    gradlew runBlockly
)
