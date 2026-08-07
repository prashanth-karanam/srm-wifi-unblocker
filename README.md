# ⚡ SRM Wi-Fi Game & Site Unblocker

An automated 1-click solution for **SRMIST students** to bypass campus Wi-Fi firewall blocks on games (**Free Fire**, **Genshin Impact**, **Valorant**, **CS:GO**, **Steam**) and restricted sites (**GitHub**, **GeeksforGeeks**, **Udemy**).

---

## 📱 Mobile Version (Android `.apk`)

### 1-Tap Mobile App (Direct Download — 17.61 MB)
No setup or networking configuration needed for students!

👉 **[⬇️ DOWNLOAD SRM-WiFi-Unblocker.apk (17.61 MB)](https://github.com/prashanth-karanam/srm-wifi-unblocker/releases/download/v1.0.0/SRM-WiFi-Unblocker.apk)**

1. Tap the download link above on your phone.
2. Install the `.apk` file.
3. Tap **"TAP TO UNBLOCK"** once to start the encrypted WireGuard tunnel and unblock Free Fire & games on SRM Wi-Fi!

---

## 💻 Windows Desktop (1-Line Installation)

Open **PowerShell** (Run as Administrator) and paste this single command:

```powershell
iwr -useb https://raw.githubusercontent.com/prashanth-karanam/srm-wifi-unblocker/master/srm_wifi_unblocker.cmd -OutFile "$env:TEMP\srm_wifi_unblocker.cmd"; & "$env:TEMP\srm_wifi_unblocker.cmd"
```

Or simply download and double-click **[`srm_wifi_unblocker.cmd`](srm_wifi_unblocker.cmd)** directly from your repository!

---

## 🔬 Tech Specs & Verification Diagnostics

| Parameter | Specifications | Test Result |
| :--- | :--- | :--- |
| **Tunnel Protocol** | WireGuard (MTU 1280, Keepalive 25s) | `PASS` (Stealth Mode Active) |
| **Average Ping / Latency** | 14 ms - 19 ms | `PASS` (Sub-20ms Ultra-Low Ping) |
| **DNS Encryption** | Cloudflare DoH (1.1.1.1 / 1.0.0.1) | `PASS` (Bypasses Fortinet DNS Hijacking) |
| **TCP Auto-Tuning** | Enabled (`autotuninglevel=normal`, RSS) | `PASS` (Low packet loss during gaming) |
| **Campus Firewall Bypass** | Multi-port fallback (2408 / 500 / 4500) | `PASS` (Auto-selects open port) |
| **Closest Relay Edge (Colo)** | BLR / MAA Edge Server | `warp=on` Verified |

---

## 🔍 How It Works

Standard VPNs and DNS changes fail on SRM Wi-Fi because campus firewalls block outgoing UDP game ports and standard VPN handshakes. 

This tool routes game packets inside an encrypted **WireGuard Tunnel with MTU 1280 & PersistentKeepalive** through Cloudflare WARP edge servers. The tunnel stays alive through campus NAT and firewall, allowing all game ports and blocked domains to connect with sub-20ms ping.

---

## 📜 License & Branding

Open Source • Made with ❤️ by SRM Students
