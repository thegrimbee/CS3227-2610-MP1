@echo off
setlocal
pushd "%~dp0"

if exist "data" rmdir /s /q "data"
if exist "ACTUAL.TXT" del /q "ACTUAL.TXT"

call ..\gradlew.bat -p .. clean build
if errorlevel 1 goto build_failed

java -jar ..\build\libs\manhwadexlite.jar < input.txt > ACTUAL.TXT
if errorlevel 1 goto run_failed

fc /N EXPECTED.TXT ACTUAL.TXT
if errorlevel 1 goto mismatch

echo PASS
set "EXIT_CODE=0"
goto cleanup

:build_failed
echo FAIL: Gradle build failed.
set "EXIT_CODE=1"
goto cleanup

:run_failed
echo FAIL: CLI execution failed.
set "EXIT_CODE=1"
goto cleanup

:mismatch
echo FAIL: Output differs from EXPECTED.TXT.
set "EXIT_CODE=1"

:cleanup
if exist "ACTUAL.TXT" del /q "ACTUAL.TXT"
if exist "data" rmdir /s /q "data"
popd
endlocal & exit /b %EXIT_CODE%
