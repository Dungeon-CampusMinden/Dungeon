@echo off
set /p answer=Web Version (yes/no)?

if /I "%answer%"=="yes" (
    echo Starting Web Version...
    call gradlew runBlockly -Pweb=true ^
        && echo Gradle stopped.

    echo Starting Blockly frontend...
    cd blockly\frontend
    call npm run dev

    echo Waiting 5 seconds before opening browser...
    timeout /t 5 /nobreak >nul

    start http://localhost:5173/

    echo Press CTRL+C to stop everything.
    pause
) else (
    echo Starting Java Dungeon...
    gradlew runBlockly
)

pause
