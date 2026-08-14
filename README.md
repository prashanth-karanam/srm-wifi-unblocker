# SRM Wi-Fi Unblocker and Setup Guide

A simple, transparent guide and automation script to fix campus Wi-Fi restrictions (SRMIST) using official Cloudflare 1.1.1.1 WARP technology.

---

## Important Clarification and Transparency

First, I want to clarify something honestly: In my previous Reddit post and earlier documentation, I had a slight misconception about how Cloudflare WARP works compared to custom scripts.

### The Plain Truth

- This project does NOT use a custom VPN server or secret proprietary VPN engine.
- It uses official, free Cloudflare 1.1.1.1 WARP infrastructure.
- Why this project exists: On campus Wi-Fi (Fortinet firewalls), Cloudflare WARP's default setting (WireGuard over UDP) gets blocked or throttled, causing high ping, dropped handshakes, or connection timeouts.

### What This Repository Provides

1. A step-by-step guide on how to change Cloudflare WARP settings manually in the official app.
2. A 1-click Windows script that automates protocol switching (MASQUE / TLS 443), DNS flushing, and TCP stack optimization for low-ping gaming and site access.

---

## Download Links

- Desktop App (Windows / macOS / Linux): https://1.1.1.1/
- Android Mobile App (Uptodown Direct APK Download): https://1-1-1-1.en.uptodown.com/android/download

### Android Note

Since the 1.1.1.1 app is not available on the Play Store, use the Uptodown APK download link above to download and install the mobile app directly.

---

## Option 1: Manual Setup Guide (Fix Official WARP Yourself)

If you prefer using the official Cloudflare WARP app directly without running any scripts, follow these steps to bypass campus blocks:

1. Open the Cloudflare 1.1.1.1 App on your Windows, Mac, or mobile device.
2. Open Settings / Preferences (click the gear icon).
3. Go to Advanced -> Connection / Protocol Settings.
4. Change the Tunnel Protocol from WireGuard (Default) to MASQUE or DoH (DNS over HTTPS / Port 443).
5. Ensure DNS over HTTPS is enabled.
6. Reconnect WARP. Traffic will now tunnel through HTTPS (Port 443), bypassing campus firewall UDP port blocks.

---

## Option 2: 1-Click Automated Windows Script

If manual settings do not work, or if you keep getting stale campus DNS cache and high gaming ping, use this automated 1-click script.

### What the Script Does (100% Transparent)

1. Flushes stale campus DNS (`ipconfig /flushdns`) to clear blocked domain redirects.
2. Tunes Windows TCP Stack (`netsh int tcp set global autotuninglevel=normal` and RSS) to reduce latency in games (Valorant, Free Fire, Genshin Impact, CS:GO).
3. Forces Stealth Fallback Routing over Port 443 / MASQUE.

### How to Run

1. Press Win + X and select PowerShell (Admin) or Terminal (Admin).
2. Copy and paste this single command, then press Enter:

```powershell
iwr -useb https://raw.githubusercontent.com/prashanth-karanam/srm-wifi-unblocker/master/wifi_unblocker.cmd -OutFile "$env:TEMP\wifi_unblocker.cmd"; & "$env:TEMP\wifi_unblocker.cmd"
```

3. Enjoy unblocked gaming (Valorant, Free Fire, Genshin Impact, Steam) and developer tools (GitHub, GFG, Udemy).

---

## Credits

- Core Network Engine: Powered by Cloudflare 1.1.1.1 WARP (https://1.1.1.1/)
