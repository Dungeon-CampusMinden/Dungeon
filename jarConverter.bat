@echo off
rem Create the "scripts" folder if it doesn't exist
mkdir scripts

rem Initialize a variable to store the list of copied files
set "copied_files="

rem Iterate over all passed arguments
for %%A in (%*) do (
    rem Check if the argument is a file or directory
    if exist %%A (
        rem Extract the filename from the path
        setlocal enabledelayedexpansion
        for %%F in ("%%A") do (
            set "filename=%%~nxF"
            endlocal

            rem Copy the file or directory to the "scripts" folder
            xcopy /s /i "%%A" "scripts\"

            rem Append the copied file to the list
            set "copied_files=!copied_files! %%A"
        )
    ) else (
        echo Das Argument '%%A' ist keine gültige Datei oder Verzeichnis.
    )
)

rem Create the JAR file
jar -cvf scripts.jar scripts

rem Output the list of copied files
echo Die folgenden Dateien wurden kopiert:%copied_files%

echo Die Dateien und Verzeichnisse wurden in den 'scripts'-Ordner kopiert und als 'scripts.jar' exportiert.
