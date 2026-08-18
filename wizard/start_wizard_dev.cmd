@echo off
setlocal
rem Developer-only launcher. The teacher-facing EXE remains a later milestone.
pushd "%~dp0.."
call gradlew.bat :wizard:buildWizardAuthoringJar --console=plain
if errorlevel 1 goto :failed
java -jar wizard\build\libs\DungeonWizard.jar
set "WIZARD_EXIT=%ERRORLEVEL%"
popd
exit /b %WIZARD_EXIT%

:failed
set "WIZARD_EXIT=%ERRORLEVEL%"
popd
exit /b %WIZARD_EXIT%
