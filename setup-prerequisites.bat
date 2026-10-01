@echo off
setlocal EnableDelayedExpansion

title ExpiryWise Setup

echo.
echo ==========================================
echo          ExpiryWise - Setup
echo ==========================================
echo.

echo [1/3] Checking Java 21...
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Java 21 not found. Installing...
    winget install Microsoft.OpenJDK.21 --accept-package-agreements --accept-source-agreements
    if %ERRORLEVEL% NEQ 0 (
        echo Failed to install Java 21.
        echo Please install Java 21 manually.
    ) else (
        echo Java 21 installed successfully.
    )
) else (
    echo Java 21 is already installed.
)

echo.
echo [2/3] Checking Maven...
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Maven not found. Installing...
    winget install Apache.Maven --accept-package-agreements --accept-source-agreements
    if %ERRORLEVEL% NEQ 0 (
        echo Failed to install Maven.
        echo Please install Maven manually.
    ) else (
        echo Maven installed successfully.
    )
) else (
    echo Maven is already installed.
)

echo.
echo [3/3] Refreshing environment...
for /f "tokens=2*" %%A in ('reg query "HKLM\System\CurrentControlSet\Control\Session Manager\Environment" /v Path 2^>nul') do set "SYS_PATH=%%B"
for /f "tokens=2*" %%A in ('reg query "HKCU\Environment" /v Path 2^>nul') do set "USR_PATH=%%B"
set "PATH=%SYS_PATH%;%USR_PATH%;%PATH%"

echo Setup complete.
echo.

set /p RUN_CHOICE="Run ExpiryWise now? [Y/N]: "

if /i "%RUN_CHOICE%"=="Y" (
    echo.
    echo Starting ExpiryWise...
    mvn clean javafx:run
) else (
    echo.
    echo Run "mvn clean javafx:run" to start ExpiryWise.
)

echo.
pause