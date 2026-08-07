# ⚡ SRM Wi-Fi Game & Site Unblocker

An automated 1-click solution for **SRMIST students** to bypass campus Wi-Fi firewall blocks on games (**Free Fire**, **Genshin Impact**, **Valorant**, **CS:GO**, **Steam**) and restricted sites (**GitHub**, **GeeksforGeeks**, **Udemy**).

---

## 📱 Mobile Version (Android `.apk`)

### 1-Tap Mobile App
No setup or networking configuration needed for students!

1. Download **`srm-wifi-unblocker-mobile.apk`** from the [GitHub Releases / Actions artifacts](../../actions).
2. Install the APK on your Android phone.
3. Tap **"TAP TO UNBLOCK"** once to start the stealth tunnel over Port 443.

---

## 💻 Windows Desktop (1-Line Super-Easy Installation)

Open **PowerShell** (Run as Administrator) and paste this single command:

```powershell
irm https://raw.githubusercontent.com/prashanth-karanam/srm-wifi-unblocker/master/srm_wifi_unblocker.cmd -OutFile "$env:USERPROFILE\Desktop\srm_wifi_unblocker.cmd"; & "$env:USERPROFILE\Desktop\srm_wifi_unblocker.cmd"
```

Or simply download and double-click **[`srm_wifi_unblocker.cmd`](srm_wifi_unblocker.cmd)** directly from your Desktop!

---

## 🔍 How It Works

Standard VPNs and DNS changes fail on SRM Wi-Fi because campus firewalls block outgoing UDP game ports and standard VPN handshakes. 

This tool routes game packets inside an encrypted **MASQUE / Stealth TLS Tunnel over Port 443**. To the campus firewall, the traffic looks identical to standard HTTPS web browsing, allowing all game ports and blocked domains to pass cleanly with sub-15ms ping.

---

## 📜 License & Branding

Open Source • Made with ❤️ by SRM Students
