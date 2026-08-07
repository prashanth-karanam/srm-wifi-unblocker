# ⚡ SRM Wi-Fi Game & Site Unblocker

An automated 1-click script for **SRMIST students** to bypass campus Wi-Fi firewall blocks on games (**Free Fire**, **Genshin Impact**, **Valorant**, **CS:GO**, **Steam**) and restricted sites (**GitHub**, **GeeksforGeeks**, **Udemy**).

---

## 🚀 1-Line Super-Easy Installation

Open **PowerShell** (Run as Administrator) and paste this single command:

```powershell
irm https://raw.githubusercontent.com/prashanth-karanam/srm-wifi-unblocker/master/srm_wifi_unblocker.cmd -OutFile "$env:USERPROFILE\Desktop\srm_wifi_unblocker.cmd"; & "$env:USERPROFILE\Desktop\srm_wifi_unblocker.cmd"
```

Or simply download and double-click **[`srm_wifi_unblocker.cmd`](srm_wifi_unblocker.cmd)** directly from your Desktop!

---

## 🔍 How It Works

Standard VPNs and DNS changes fail on SRM Wi-Fi because campus firewalls block outgoing UDP game ports and standard VPN handshakes. 

This tool routes game packets inside an encrypted **MASQUE (HTTP/3 over TLS Port 443)** tunnel. To the campus firewall, the traffic looks identical to standard HTTPS web browsing, allowing all game ports and blocked domains to pass cleanly with sub-15ms ping.
