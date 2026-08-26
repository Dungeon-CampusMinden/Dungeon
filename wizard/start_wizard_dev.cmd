@echo off
setlocal
rem Developer launcher for the Wizard authoring host. Requires Java 25.
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
