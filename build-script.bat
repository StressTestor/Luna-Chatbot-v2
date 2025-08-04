@echo off
REM Simple build script for Luna Chatbot Android project
REM This serves as our MCP build server functionality

echo Starting build process...

REM Check if gradlew.bat exists
if not exist "gradlew.bat" (
    echo Error: gradlew.bat not found
    exit /b 1
)

REM Download gradle wrapper if needed
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo Downloading gradle wrapper...
    powershell -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.13-bin.zip' -OutFile 'gradle-temp.zip'"
    powershell -Command "Expand-Archive -Path 'gradle-temp.zip' -DestinationPath 'gradle-temp'"
    copy "gradle-temp\gradle-8.13\lib\gradle-wrapper.jar" "gradle\wrapper\gradle-wrapper.jar"
    rmdir /s /q gradle-temp
    del gradle-temp.zip
)

REM Run the build command based on parameter
if "%1"=="clean" (
    echo Running clean...
    call gradlew.bat clean
) else if "%1"=="build" (
    echo Running build...
    call gradlew.bat assembleDebug
) else if "%1"=="test" (
    echo Running tests...
    call gradlew.bat testDebugUnitTest
) else (
    echo Usage: build-script.bat [clean^|build^|test]
    echo Available commands:
    echo   clean - Clean build artifacts
    echo   build - Build debug APK
    echo   test  - Run unit tests
)

echo Build process completed.