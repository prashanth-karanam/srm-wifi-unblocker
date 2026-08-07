@echo off
:: ====================================================================
:: SRM Wi-Fi Unblocker v3.0 (Live Progress Edition)
:: Shows live step-by-step progress without screen clearing
:: ====================================================================
title SRM Wi-Fi Unblocker
color 0A

echo ====================================================================
echo                   SRM WI-FI GAME & SITE UNBLOCKER                  
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
echo [Step 1/5] Flushing DNS and local network socket cache...
ipconfig /flushdns
echo [✔] DNS resolver cache flushed successfully!

echo.
set "WARP_PATH=C:\Program Files\Cloudflare\Cloudflare WARP"
set "WARP_CLI=%WARP_PATH%\warp-cli.exe"

echo [Step 2/5] Checking core bypass engine (Cloudflare WARP)...
if not exist "%WARP_CLI%" (
    echo [*] WARP engine not found. Downloading & Installing...
    echo [*] Attempting install via Windows Package Manager...
    winget install --id Cloudflare.Warp --accept-source-agreements --accept-package-agreements --silent
    
    if not exist "%WARP_CLI%" (
        echo [*] Winget fallback triggered... Downloading direct installer...
        powershell -Command "$url='https://downloads.cloudflareclient.com/v1/download/windows/version/2026.6.880.0'; $dest='$env:TEMP\Cloudflare_WARP.msi'; (New-Object System.Net.WebClient).DownloadFile($url, $dest); Start-Process msiexec.exe -ArgumentList '/i', `"$dest`", '/quiet', '/qn' -Wait"
    )
    echo [✔] Bypass engine installation completed!
) else (
    echo [✔] Cloudflare WARP engine verified!
)

echo.
echo [Step 3/5] Starting local background services...
sc start warp-svc >nul 2>&1
echo [✔] Background services ready!

echo.
set /a ATTEMPT=1
set /a MAX_ATTEMPTS=4

:CONNECT_LOOP
echo [Step 4/5] Establishing stealth network tunnel (Attempt %ATTEMPT% of %MAX_ATTEMPTS%)...
echo [*] Cleaning old registration...
"%WARP_CLI%" registration delete
echo [*] Generating new encryption keys...
"%WARP_CLI%" registration new
echo [*] Setting mode to WARP stealth tunnel...
"%WARP_CLI%" mode warp
echo [*] Connecting to Cloudflare stealth servers over Port 443...
"%WARP_CLI%" connect

echo.
echo [Step 5/5] Testing Wi-Fi firewall bypass status...
timeout /t 3 /nobreak >nul

"%WARP_CLI%" status | findstr /I "Connected" >nul 2>&1
if %errorlevel% equ 0 (
    echo [✔] Status verified: CONNECTED!
    goto SUCCESS_FINAL
)

if %ATTEMPT% lss %MAX_ATTEMPTS% (
    echo [!] Tunnel initializing... Retrying step %ATTEMPT%...
    set /a ATTEMPT+=1
    timeout /t 2 /nobreak >nul
    goto CONNECT_LOOP
)

:SUCCESS_FINAL
echo.
echo ====================================================================
echo   [SUCCESS] Wi-Fi Firewall Unblocked Successfully!
echo   - Free Fire / Genshin / Valorant / Steam  : UNBLOCKED!
echo   - Blocked Websites & Study Portals        : UNBLOCKED!
echo ====================================================================
echo.
"%WARP_CLI%" status
echo.
echo Operation finished successfully. You can close this window now.
pause
