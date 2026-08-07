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

    private val prefs: SharedPreferences = context.getSharedPreferences("warp_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun getOrGenerateConfig(): Config = withContext(Dispatchers.IO) {
        val savedPrivateKey = prefs.getString("private_key", null)
        val savedAddressV4 = prefs.getString("address_v4", null)
        val savedPeerPublicKey = prefs.getString("peer_public_key", null)
        val savedEndpoint = prefs.getString("endpoint", null)

        if (savedPrivateKey != null && savedAddressV4 != null && savedPeerPublicKey != null && savedEndpoint != null) {
            return@withContext buildWireGuardConfig(savedPrivateKey, savedAddressV4, savedPeerPublicKey, savedEndpoint)
        }

        // Register new Cloudflare WARP account
        val keyPair = KeyPair()
        val privateKeyHex = keyPair.privateKey.toBase64()
        val publicKeyHex = keyPair.publicKey.toBase64()

        val jsonBody = JSONObject().apply {
            put("key", publicKeyHex)
            put("type", "Android")
            put("locale", "en_US")
            put("tos", "2019-09-05T00:00:00.000+02:00")
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
                val addressV4 = addressesObj.getString("v4")
                
                val peersArr = configObj.getJSONArray("peers")
                val peerObj = peersArr.getJSONObject(0)
                val peerPublicKey = peerObj.getString("public_key")
                val endpointObj = peerObj.getJSONObject("endpoint")
                val endpointHost = endpointObj.getString("host")

                // Save credentials
                prefs.edit().apply {
                    putString("private_key", privateKeyHex)
                    putString("address_v4", addressV4)
                    putString("peer_public_key", peerPublicKey)
                    putString("endpoint", endpointHost)
                    apply()
                }

                return@withContext buildWireGuardConfig(privateKeyHex, addressV4, peerPublicKey, endpointHost)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback default Cloudflare WARP parameters
        val fallbackPrivKey = privateKeyHex
        val fallbackAddr = "172.16.0.2/32"
        val fallbackPeerPubKey = "bmXOC+F1gEMF9vhTOHwDHnuBSpqB2ioOH32IkSJxcGQ="
        val fallbackEndpoint = "162.159.192.1:2408"

        return@withContext buildWireGuardConfig(fallbackPrivKey, fallbackAddr, fallbackPeerPubKey, fallbackEndpoint)
    }

    private fun buildWireGuardConfig(
        privateKeyStr: String,
        addressV4Str: String,
        peerPublicKeyStr: String,
        endpointStr: String
    ): Config {
        val builder = Config.Builder()

        // Interface configuration
        val ifaceBuilder = Interface.Builder()
            .parsePrivateKey(privateKeyStr)
            .addAddress(InetNetwork.parse(addressV4Str))
            .addDnsServer(InetAddress.getByName("1.1.1.1"))
            .addDnsServer(InetAddress.getByName("1.0.0.1"))
            
        builder.setInterface(ifaceBuilder.build())

        // Peer configuration
        val peerBuilder = Peer.Builder()
            .parsePublicKey(peerPublicKeyStr)
            .addAllowedIp(InetNetwork.parse("0.0.0.0/0"))
            .parseEndpoint(if (endpointStr.contains(":")) endpointStr else "$endpointStr:2408")

        builder.addPeer(peerBuilder.build())

        return builder.build()
    }
}
