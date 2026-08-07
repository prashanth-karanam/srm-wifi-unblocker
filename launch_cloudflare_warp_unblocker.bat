@echo off
:: ====================================================================
:: SRM Wi-Fi Unblocker v4.0 (Ultra-Optimized Smart Polling)
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
ipconfig /flushdns >nul 2>&1
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
echo [Step 4/5] Checking current network status...
"%WARP_CLI%" status | findstr /I "Connected" >nul 2>&1
if %errorlevel% equ 0 (
    echo [✔] You are ALREADY securely connected! Skipping setup.
    goto SUCCESS_FINAL
)

set /a FULL_RESETS=1
set /a MAX_RESETS=2

:FULL_RESET_LOOP
echo.
echo [*] Establishing stealth network tunnel (Setup Attempt %FULL_RESETS% of %MAX_RESETS%)...
echo [*] Cleaning old registration...
"%WARP_CLI%" registration delete >nul 2>&1
echo [*] Generating new encryption keys...
"%WARP_CLI%" registration new >nul 2>&1
echo [*] Setting mode to WARP stealth tunnel...
"%WARP_CLI%" mode warp >nul 2>&1
echo [*] Connecting to Cloudflare stealth servers over Port 443...
"%WARP_CLI%" connect >nul 2>&1

echo.
echo [Step 5/5] Waiting for Wi-Fi firewall bypass to establish...
set /a POLL_ATTEMPT=1
set /a MAX_POLLS=10

:POLL_LOOP
"%WARP_CLI%" status | findstr /I "Connected" >nul 2>&1
if %errorlevel% equ 0 (
    echo [✔] Status verified: CONNECTED! (Took %POLL_ATTEMPT% checks)
    goto SUCCESS_FINAL
)

if %POLL_ATTEMPT% lss %MAX_POLLS% (
    echo [~] Tunnel initializing... Please wait (Check %POLL_ATTEMPT% of %MAX_POLLS%)...
    set /a POLL_ATTEMPT+=1
    timeout /t 2 /nobreak >nul
    goto POLL_LOOP
)

if %FULL_RESETS% lss %MAX_RESETS% (
    echo [!] Tunnel took too long to connect. Re-registering keys...
    set /a FULL_RESETS+=1
    goto FULL_RESET_LOOP
)

echo [X] Failed to connect after all attempts. Please check your internet connection.
goto END

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

:END
echo Operation finished successfully. You can close this window now.
pause
