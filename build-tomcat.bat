@echo off
setlocal enabledelayedexpansion

set JAVA_HOME=D:\Program Files\ojdkbuild\java-1.8.0-openjdk-1.8.0.302-1
set PROJECT_DIR=%~dp0
set SRC_DIR=%PROJECT_DIR%src
set WEB_DIR=%PROJECT_DIR%web
set LIB_DIR=%PROJECT_DIR%lib
set CLASSES_DIR=%WEB_DIR%\WEB-INF\classes
set TOMCAT_APPS=D:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\zhgl

echo ============================================
echo   Enterprise Account Manager - Build + Deploy
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
    
    :: Deploy to Tomcat
    echo Deploying to Tomcat...
    :: Backup database before cleanup
    set DB_FILE=%TOMCAT_APPS%\WEB-INF\accountant.db
    if exist "%DB_FILE%" copy "%DB_FILE%" "%TEMP%\accountant.db" >nul
    :: Clean and redeploy
    if exist "%TOMCAT_APPS%" rmdir /s /q "%TOMCAT_APPS%"
    mkdir "%TOMCAT_APPS%"
    xcopy /e /i /y "%WEB_DIR%\*" "%TOMCAT_APPS%"
    :: Restore database
    if exist "%TEMP%\accountant.db" (
        if not exist "%TOMCAT_APPS%\WEB-INF" mkdir "%TOMCAT_APPS%\WEB-INF"
        copy "%TEMP%\accountant.db" "%DB_FILE%" >nul
        del "%TEMP%\accountant.db"
    )
    echo.
    echo ============================================
    echo   DEPLOY COMPLETE
    echo ============================================
    echo.
    echo App deployed to: %TOMCAT_APPS%
    echo.
) else (
    echo.
    echo ============================================
    echo   BUILD FAILED (Error level: %ERRORLEVEL%)
    echo ============================================
    echo.
)

pause
