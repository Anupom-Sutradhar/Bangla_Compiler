@echo off
setlocal

echo Cleaning generated files...

REM Remove artifacts produced by older versions of the project from the root.
del /q *.class 2>nul
del /q Output.java 2>nul

REM Remove the current clean build/output directory.
if exist build rmdir /s /q build

echo Clean complete. Source files were not changed.
endlocal
