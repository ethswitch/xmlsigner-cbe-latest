@echo off
setlocal enabledelayedexpansion

:: Configuration
set DOCKER_USERNAME=take2018
set DOCKER_IMAGE_NAME=take2018/xml-signer-app
set DOCKER_IMAGE_TAG=threaded_0.1

set FULL_IMAGE_NAME=take2018/xml-signer-app:threaded_0.1

echo Logging into Docker Hub...
docker login -u %DOCKER_USERNAME% -p ILtektesf701389
if errorlevel 1 (
    echo ❌ Docker login failed.
    exit /b 1
)

echo Building Docker image...
docker build -t %FULL_IMAGE_NAME% .
if errorlevel 1 (
    echo ❌ Docker build failed.
    exit /b 1
)

echo Pushing Docker image to Docker Hub...
docker push %FULL_IMAGE_NAME%
if errorlevel 1 (
    echo ❌ Docker push failed.
    exit /b 1
)

echo ✅ Successfully pushed %FULL_IMAGE_NAME%
endlocal
