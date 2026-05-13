@echo off
setlocal enabledelayedexpansion

set JAVA_HOME=D:\Program Files\ojdkbuild\java-1.8.0-openjdk-1.8.0.302-1
set PROJECT_DIR=%~dp0
set SRC_DIR=%PROJECT_DIR%src
set WEB_DIR=%PROJECT_DIR%web
set LIB_DIR=%PROJECT_DIR%lib
set CLASSES_DIR=%WEB_DIR%\WEB-INF\classes

echo ============================================
echo   Enterprise Account Manager - Build Script
echo ============================================

:: Build classpath from lib jars
set CLASSPATH=
for %%j in ("%LIB_DIR%\*.jar") do (
    if "!CLASSPATH!"=="" (
        set CLASSPATH=%%j
    ) else (
        set CLASSPATH=!CLASSPATH!;%%j
    )
)

echo Classpath: %CLASSPATH%
echo.

:: Clean and recreate classes directory
if exist "%CLASSES_DIR%" rmdir /s /q "%CLASSES_DIR%"
mkdir "%CLASSES_DIR%"

:: Compile all Java source files
echo Compiling Java sources...
dir /s /b "%SRC_DIR%\*.java" > "%PROJECT_DIR%sources.txt"
"%JAVA_HOME%\bin\javac" -encoding UTF-8 -cp "%CLASSPATH%" -d "%CLASSES_DIR%" @"%PROJECT_DIR%sources.txt"
del "%PROJECT_DIR%sources.txt"

if %ERRORLEVEL% equ 0 (
    echo.
    echo ============================================
    echo   BUILD SUCCESSFUL
    echo ============================================
    echo.
    echo Output: %CLASSES_DIR%
    echo.
    echo To deploy, copy the "web" folder to Tomcat webapps/
    echo and rename it (e.g., "accountant").
    echo Then access: http://localhost:8080/accountant/
    echo.
) else (
    echo.
    echo ============================================
    echo   BUILD FAILED (Error level: %ERRORLEVEL%)
    echo ============================================
    echo.
)

pause
