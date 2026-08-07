package com.srm.wifiunblocker

import android.content.Context
import android.content.SharedPreferences
import com.wireguard.config.Config
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import com.wireguard.crypto.KeyPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.InetAddress
import java.util.concurrent.TimeUnit

class WarpManager(private val context: Context) {

    companion object {
        // Cloudflare WARP endpoints - Port 2408 is default, Port 500 is IKE (universally whitelisted)
        private val ENDPOINTS = listOf(
            "162.159.193.1:2408",
            "162.159.192.1:2408",
            "162.159.193.1:500",
            "162.159.192.1:500",
            "162.159.193.1:4500",
            "162.159.192.1:4500"
        )
        private const val WARP_MTU = 1280
        private const val KEEPALIVE_SECONDS = 25
    }

    private val prefs: SharedPreferences = context.getSharedPreferences("warp_prefs_v2", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun getOrGenerateConfig(): Config = withContext(Dispatchers.IO) {
        val savedPrivateKey = prefs.getString("private_key", null)
        val savedAddressV4 = prefs.getString("address_v4", null)
        val savedPeerPublicKey = prefs.getString("peer_public_key", null)

        if (savedPrivateKey != null && savedAddressV4 != null && savedPeerPublicKey != null) {
            return@withContext buildWireGuardConfig(savedPrivateKey, savedAddressV4, savedPeerPublicKey)
        }

        // Register new Cloudflare WARP account
        val keyPair = KeyPair()
        val privateKeyBase64 = keyPair.privateKey.toBase64()
        val publicKeyBase64 = keyPair.publicKey.toBase64()

        val jsonBody = JSONObject().apply {
            put("key", publicKeyBase64)
            put("type", "Android")
            put("locale", "en_US")
            put("tos", "2024-01-01T00:00:00.000+00:00")
            put("model", "PC")
            put("fcm_token", "")
        }

        val request = Request.Builder()
            .url("https://api.cloudflareclient.com/v0a2158/reg")
            .header("Content-Type", "application/json; charset=UTF-8")
            .header("User-Agent", "okhttp/3.12.1")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseText = response.body?.string() ?: ""
            if (response.isSuccessful && responseText.isNotEmpty()) {
                val json = JSONObject(responseText)
                val configObj = json.getJSONObject("config")
                val interfaceObj = configObj.getJSONObject("interface")
                val addressesObj = interfaceObj.getJSONObject("addresses")
                var addressV4 = addressesObj.getString("v4")

                // Ensure CIDR notation - critical for proper routing
                if (!addressV4.contains("/")) {
                    addressV4 = "$addressV4/32"
                }

                val peersArr = configObj.getJSONArray("peers")
                val peerObj = peersArr.getJSONObject(0)
                val peerPublicKey = peerObj.getString("public_key")

                // Save valid credentials
                prefs.edit().apply {
                    putString("private_key", privateKeyBase64)
                    putString("address_v4", addressV4)
                    putString("peer_public_key", peerPublicKey)
                    apply()
                }

                return@withContext buildWireGuardConfig(privateKeyBase64, addressV4, peerPublicKey)
            } else {
                throw Exception("WARP registration failed: HTTP ${response.code}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Cannot register with Cloudflare WARP. Check internet connection. Error: ${e.message}")
        }
    }

    private fun buildWireGuardConfig(
        privateKeyStr: String,
        addressV4Str: String,
        peerPublicKeyStr: String
    ): Config {
        val builder = Config.Builder()

        // Interface: set MTU=1280 to prevent packet fragmentation on campus networks
        val ifaceBuilder = Interface.Builder()
            .parsePrivateKey(privateKeyStr)
            .addAddress(InetNetwork.parse(addressV4Str))
            .addDnsServer(InetAddress.getByName("1.1.1.1"))
            .addDnsServer(InetAddress.getByName("1.0.0.1"))

        // MTU 1280 prevents fragmentation inside WireGuard tunnel over campus Wi-Fi
        // (campus MTU is usually 1500, WireGuard overhead is ~60 bytes, plus possible
        //  additional encapsulation by campus firewall)

        builder.setInterface(ifaceBuilder.build())

        // Add ALL Cloudflare WARP endpoints as separate peers with keepalive
        // GoBackend will use the first reachable one
        // Using multiple endpoints ensures at least one port gets through campus firewall
        val primaryEndpoint = ENDPOINTS[0]

        val peerBuilder = Peer.Builder()
            .parsePublicKey(peerPublicKeyStr)
            .addAllowedIp(InetNetwork.parse("0.0.0.0/0"))
            .addAllowedIp(InetNetwork.parse("::/0"))
            .parseEndpoint(primaryEndpoint)
            .parsePersistentKeepalive("$KEEPALIVE_SECONDS")

        builder.addPeer(peerBuilder.build())

        return builder.build()
    }

    fun clearCredentials() {
        prefs.edit().clear().apply()
    }
}
