@echo off
chcp 65001 > nul
setlocal

REM ============================================================
REM Bangla Compiler - Clean Build Runner (Windows)
REM All generated .class/.java files go under build\
REM Usage:
REM   run.bat
REM   run.bat tests\valid_full.bng
REM   run.bat my_program.bng
REM ============================================================

if "%~1"=="" (
    set "SOURCE=input.bng"
    set "PROGRAM=input"
) else (
    set "SOURCE=%~1"
    set "PROGRAM=%~n1"
)

if not exist "%SOURCE%" (
    echo ERROR: Source file not found: %SOURCE%
    exit /b 1
)

set "COMPILER_BUILD=build\compiler"
set "RUN_BUILD=build\runs\%PROGRAM%"

if not exist "%COMPILER_BUILD%" mkdir "%COMPILER_BUILD%"
if not exist "%RUN_BUILD%" mkdir "%RUN_BUILD%"

REM Remove only old generated files for this program so stale output is never reused.
del /q "%RUN_BUILD%\Output.java" "%RUN_BUILD%\Output.class" 2>nul

echo [1/4] Compiling Bangla compiler...
javac -encoding UTF-8 -d "%COMPILER_BUILD%" Main.java TokenType.java Token.java Diagnostic.java ValueType.java Lexer.java Ast.java Parser.java SemanticAnalyzer.java CodeGenerator.java
if errorlevel 1 goto :fail

echo [2/4] Compiling %SOURCE% to %RUN_BUILD%\Output.java...
java -cp "%COMPILER_BUILD%" Main "%SOURCE%" "%RUN_BUILD%\Output.java"
if errorlevel 1 goto :fail

echo [3/4] Compiling generated Java...
javac -encoding UTF-8 -d "%RUN_BUILD%" "%RUN_BUILD%\Output.java"
if errorlevel 1 goto :fail

echo [4/4] Running generated program...
echo ----------------------------------------
java -cp "%RUN_BUILD%" Output
if errorlevel 1 goto :fail
echo ----------------------------------------

echo.
echo Done successfully.
echo Generated files are inside: %RUN_BUILD%
exit /b 0

:fail
echo.
echo Build/run failed. Read the error message above.
echo Generated/build files stay inside the build folder only.
exit /b 1
