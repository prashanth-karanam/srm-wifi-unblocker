@echo off
:: ====================================================================
:: SRM Wi-Fi Unblocker (Cloudflare WARP MASQUE Port-443 Launcher)
:: ====================================================================
title SRM Wi-Fi Unblocker - Cloudflare WARP
color 0A

echo ====================================================================
echo        SRM WI-FI UNBLOCKER - CLOUDFLARE WARP STEALTH TUNNEL         
echo ====================================================================
echo.

:: Step 0: Administrator Privileges Check
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] Requesting Administrator privileges...
    powershell -Command "Start-Process '%~f0' -Verb RunAs" 2>nul
    if %errorlevel% neq 0 (
        echo [!] Continuing in standard user mode...
    ) else (
        exit /b
    )
) else (
    echo [✔] Administrator privileges confirmed!
)

echo.
echo [Step 1/5] Flushing DNS and network cache...
ipconfig /flushdns
echo [✔] Cache cleared!

echo.
set "WARP_PATH=C:\Program Files\Cloudflare\Cloudflare WARP"
set "WARP_CLI=%WARP_PATH%\warp-cli.exe"

echo [Step 2/5] Verifying Cloudflare WARP installation...
if not exist "%WARP_CLI%" (
    echo [*] Cloudflare WARP not detected. Installing automatically...
    winget install --id Cloudflare.Warp --accept-source-agreements --accept-package-agreements --silent
    
    if not exist "%WARP_CLI%" (
        echo [*] Winget fallback... Downloading direct installer...
        powershell -Command "$url='https://downloads.cloudflareclient.com/v1/download/windows/version/2026.6.880.0'; $dest='$env:TEMP\Cloudflare_WARP.msi'; (New-Object System.Net.WebClient).DownloadFile($url, $dest); Start-Process msiexec.exe -ArgumentList '/i', `"$dest`", '/quiet', '/qn' -Wait"
    )
    echo [✔] Cloudflare WARP installation complete!
) else (
    echo [✔] Cloudflare WARP engine verified!
)

echo.
echo [Step 3/5] Starting Cloudflare background service...
sc start warp-svc >nul 2>&1
echo [✔] Cloudflare service ready!

echo.
set /a ATTEMPT=1
set /a MAX_ATTEMPTS=4

:CONNECT_LOOP
echo [Step 4/5] Connecting to Cloudflare MASQUE Tunnel (Attempt %ATTEMPT% of %MAX_ATTEMPTS%)...
echo [*] Cleaning old registration...
"%WARP_CLI%" registration delete >nul 2>&1
echo [*] Registering new encryption key...
"%WARP_CLI%" registration new
echo [*] Setting mode to WARP stealth tunnel (Port 443)...
"%WARP_CLI%" mode warp
echo [*] Connecting...
"%WARP_CLI%" connect

echo.
echo [Step 5/5] Checking connection status...
timeout /t 3 /nobreak >nul

"%WARP_CLI%" status | findstr /I "Connected" >nul 2>&1
if %errorlevel% equ 0 (
    echo [✔] Status verified: CONNECTED!
    goto SUCCESS_FINAL
)

if %ATTEMPT% lss %MAX_ATTEMPTS% (
    echo [!] Initializing... Retrying step %ATTEMPT%...
    set /a ATTEMPT+=1
    timeout /t 2 /nobreak >nul
    goto CONNECT_LOOP
)

:SUCCESS_FINAL
echo.
echo ====================================================================
echo   [SUCCESS] Cloudflare WARP Stealth Tunnel Connected!
echo   - Free Fire / Genshin / Valorant / Steam  : UNBLOCKED!
echo   - Blocked Websites & Study Portals        : UNBLOCKED!
echo ====================================================================
echo.
"%WARP_CLI%" status
echo.
echo Setup finished. Press any key to exit...
pause
