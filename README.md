# 🚀 SRM Wi-Fi Game & Site Unblocker

An automated 1-click script for **SRMIST students** to bypass campus Wi-Fi firewall blocks on games (**Free Fire**, **Genshin Impact**, **Valorant**, **CS:GO**, **Steam**) and restricted sites (**GitHub**, **GeeksforGeeks**, **Udemy**).

---

## ⚡ How to Use

1. Download **[`srm_wifi_unblocker.cmd`](srm_wifi_unblocker.cmd)**.
2. Double-click the file (or Right-Click -> **Run as Administrator**).
3. The script automatically handles DNS cleaning, core engine verification, and establishing a **MASQUE stealth tunnel over Port 443**.
4. Once you see **`[SUCCESS] Wi-Fi Firewall Unblocked Successfully!`**, launch your games or browser!

---

## 🔍 How It Works

Standard VPNs and DNS changes fail on SRM Wi-Fi because campus firewalls block outgoing UDP game ports and standard VPN handshakes. 

This tool routes game packets inside an encrypted **MASQUE (HTTP/3 over TLS Port 443)** tunnel. To the campus firewall, the traffic looks identical to standard HTTPS web browsing, allowing all game ports and blocked domains to pass cleanly with sub-15ms ping.
