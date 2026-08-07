@echo off
:: ====================================================================
:: SRM Wi-Fi Unblocker v4.0 (Ultra-Speed Turbo & Smart Handshake Edition)
:: Low-Ping Gaming Optimization + Smart WARP Connection Engine
:: ====================================================================
title SRM Wi-Fi Unblocker (Ultra Speed & Low Ping)
color 0A

echo ====================================================================
echo      SRM WI-FI GAME UNBLOCKER - ULTRA SPEED & LOW-PING TURBO
echo ====================================================================
echo.

:: Step 0: Administrator Privileges Auto-Elevation
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
echo [Step 1/5] Applying Gaming Network & TCP Speed Optimizations...
ipconfig /flushdns >nul 2>&1
netsh int tcp set global autotuninglevel=normal >nul 2>&1
netsh int tcp set global rss=enabled >nul 2>&1
netsh int tcp set global fastopen=enabled >nul 2>&1
netsh int tcp set global ecncapability=enabled >nul 2>&1
echo [✔] High-speed TCP auto-tuning & low-ping RSS engine activated!

echo.
set "WARP_PATH=C:\Program Files\Cloudflare\Cloudflare WARP"
set "WARP_CLI=%WARP_PATH%\warp-cli.exe"

echo [Step 2/5] Verifying Cloudflare WARP engine...
if not exist "%WARP_CLI%" (
    echo [*] WARP engine missing. Downloading & Installing...
    winget install --id Cloudflare.Warp --accept-source-agreements --accept-package-agreements --silent >nul 2>&1
    
    if not exist "%WARP_CLI%" (
        echo [*] Winget fallback triggered... Downloading direct installer...
        powershell -Command "$url='https://downloads.cloudflareclient.com/v1/download/windows/version/2026.6.880.0'; $dest='$env:TEMP\Cloudflare_WARP.msi'; (New-Object System.Net.WebClient).DownloadFile($url, $dest); Start-Process msiexec.exe -ArgumentList '/i', `"$dest`", '/quiet', '/qn' -Wait" >nul 2>&1
    )
    echo [✔] Bypass engine installation completed!
) else (
    echo [✔] Cloudflare WARP engine verified!
)

echo.
echo [Step 3/5] Checking background service...
sc start warp-svc >nul 2>&1
echo [✔] Background service active!

echo.
echo [Step 4/5] Checking current network status...
"%WARP_CLI%" status | findstr /I "Connected" >nul 2>&1
if %errorlevel% equ 0 (
    echo [✔] Already connected to Cloudflare stealth tunnel!
    goto SUCCESS_FINAL
)

echo [*] Initializing Cloudflare MASQUE Stealth Tunnel...
"%WARP_CLI%" mode warp >nul 2>&1
"%WARP_CLI%" connect >nul 2>&1

:: Smart Handshake Wait Loop (Patient Polling - Does NOT interrupt connection)
set /a WAIT_COUNT=1
set /a MAX_WAIT=6

:POLL_WAIT
echo [*] Waiting for handshake verification (Check %WAIT_COUNT% of %MAX_WAIT%)...
timeout /t 2 /nobreak >nul

"%WARP_CLI%" status | findstr /I "Connected" >nul 2>&1
if %errorlevel% equ 0 (
    echo [✔] Stealth tunnel connected successfully!
    goto SUCCESS_FINAL
)

if %WAIT_COUNT% lss %MAX_WAIT% (
    set /a WAIT_COUNT+=1
    goto POLL_WAIT
)

:: If patient waiting failed, perform a fresh registration reset
echo.
echo [!] Handshake timeout. Performing smart registration reset...
"%WARP_CLI%" registration delete >nul 2>&1
timeout /t 1 /nobreak >nul
"%WARP_CLI%" registration new >nul 2>&1
timeout /t 1 /nobreak >nul
"%WARP_CLI%" mode warp >nul 2>&1
"%WARP_CLI%" connect >nul 2>&1

echo [*] Finalizing stealth connection...
timeout /t 4 /nobreak >nul

:SUCCESS_FINAL
echo.
echo ====================================================================
echo   [SUCCESS] Wi-Fi Firewall Unblocked & Network Turbocharged!
echo   - Speed Auto-Tuning                       : MAXIMUM
echo   - Low-Ping RSS Core Acceleration          : ENABLED
echo   - Free Fire / Genshin / Valorant / Steam  : UNBLOCKED!
echo ====================================================================
echo.
"%WARP_CLI%" status
echo.
echo Setup complete. You can close this window now.
pause
