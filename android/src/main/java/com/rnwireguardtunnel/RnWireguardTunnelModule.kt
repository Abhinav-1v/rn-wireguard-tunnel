package com.rnwireguardtunnel

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
import android.app.Activity
import android.content.Intent
import android.net.VpnService
import com.facebook.react.bridge.*
import com.facebook.react.bridge.*
import com.wireguard.android.backend.GoBackend
import com.wireguard.config.Config
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import com.wireguard.android.backend.Tunnel
import java.net.InetAddress
import com.wireguard.config.InetNetwork
import com.wireguard.config.ParseException
import com.wireguard.crypto.Key
import com.wireguard.crypto.KeyPair
import com.facebook.react.bridge.Promise

@ReactModule(name = RnWireguardTunnelModule.NAME)
class RnWireguardTunnelModule(reactContext: ReactApplicationContext) :
  NativeRnWireguardTunnelSpec(reactContext), ActivityEventListener {

  private var backend: GoBackend? = null
  private var tunnel: Tunnel? = null
  private var config: Config? = null

  override fun getName(): String {
    return NAME
  }
  companion object {
    const val NAME = "RnWireguardTunnel"
    private const val VPN_REQUEST_CODE = 1001
  }

  // ------------------------- VPN Methods Section Started ------------------------- //

  init {
    reactContext.addActivityEventListener(this)
  }

  private var vpnPermissionPromise: Promise? = null

  override fun requestVpnPermission(promise: Promise) {
      val activity = reactApplicationContext.currentActivity
      if (activity == null) {
          promise.reject("ACTIVITY_DOES_NOT_EXIST", "Activity doesn't exist")
          return
      }

      val intent = VpnService.prepare(activity)
      if (intent != null) {
          // Permission not granted, request it
          vpnPermissionPromise = promise
          activity.startActivityForResult(intent, VPN_REQUEST_CODE)
      } else {
          // Permission already granted
          promise.resolve(true)
      }
  }


  // Correct signature: non-null Activity
  override fun onActivityResult(
      activity: Activity,
      requestCode: Int,
      resultCode: Int,
      data: Intent?
  ) {
      if (requestCode == VPN_REQUEST_CODE && vpnPermissionPromise != null) {
          if (resultCode == Activity.RESULT_OK) {
              vpnPermissionPromise?.resolve(true)
          } else {
              vpnPermissionPromise?.reject("VPN_PERMISSION_DENIED", "User denied VPN permission")
          }
          vpnPermissionPromise = null
      }
  }

  // Correct signature: non-null Intent
  override fun onNewIntent(intent: Intent) {
      // Not needed for vpn service
  }

  // ------------------------- VPN Methods Section Ended ------------------------- //

override fun generateKeys(promise: Promise) {
    try {
        val keyPair = KeyPair()
        val result = Arguments.createMap().apply {
            putString("privateKey", keyPair.privateKey.toBase64())
            putString("publicKey", keyPair.publicKey.toBase64())
        }
        promise.resolve(result)
    } catch (e: Exception) {
        promise.reject("KEYGEN_ERROR", e.message)
    }
}


  override fun initialize(promise: Promise) {
      try {
          backend = GoBackend(reactApplicationContext)
          promise.resolve(null)
      } catch (e: Exception) {
          promise.reject("INIT_ERROR", "Failed to initialize WireGuard: ${e.message}")
      }
  }
  

  override fun connect(config: ReadableMap, promise: Promise) {
      try {
          println("Starting VPN connection process...")
          println("Received config: $config")

          if (backend == null) {
              println("Backend is null, initializing...")
              backend = GoBackend(reactApplicationContext)
          }

          val interfaceBuilder = Interface.Builder()

          // Parse private key
          val privateKey = config.getString("clientPrivateKey") ?: throw Exception("Client private key is required")
          try {
              println("Parsing client private key: $privateKey")
              interfaceBuilder.parsePrivateKey(privateKey)
              println("Client private key parsed successfully")
          } catch (e: ParseException) {
              println("Failed to parse client private key: ${e.message}")
              throw Exception("Invalid client private key format: ${e.message}, Key: $privateKey")
          }

          // Parse interface (client) address (non-nullable)
          val addressString = config.getString("clientAddress") ?: throw Exception("Missing client interface address (e.g. 10.0.0.2/32)")
          try {
              println("Parsing client interface address: $addressString")
              interfaceBuilder.addAddress(InetNetwork.parse(addressString))
              println("Client interface address parsed successfully")
          } catch (e: ParseException) {
              println("Failed to parse client interface address: ${e.message}")
              throw Exception("Invalid client interface address format: ${e.message}, Address: $addressString")
          }

          // Parse DNS servers (optional)
          if (config.hasKey("dns")) {
              val dnsArray = config.getArray("dns")
              val dnsList = mutableListOf<String>()
              if (dnsArray != null) {
                  for (i in 0 until dnsArray.size()) {
                      val maybeDns = if (!dnsArray.isNull(i)) dnsArray.getString(i) else null
                      maybeDns?.let { dnsList.add(it) }
                  }
              }
              try {
                  println("Parsing DNS servers: $dnsList")
                  dnsList.forEach { dnsString ->
                      interfaceBuilder.addDnsServer(InetAddress.getByName(dnsString))
                  }
                  println("DNS servers parsed successfully")
              } catch (e: Exception) {
                  println("Failed to parse DNS servers: ${e.message}")
                  throw Exception("Invalid DNS server format: ${e.message}")
              }
          }

          // Set MTU if provided
          if (config.hasKey("mtu")) {
              val mtu = config.getInt("mtu")
              if (mtu < 1280 || mtu > 65535) {
                  throw Exception("MTU must be between 1280 and 65535, got: $mtu")
              }
              interfaceBuilder.setMtu(mtu)
          }

          val peerBuilder = Peer.Builder()

          // Parse public key
          val publicKey = config.getString("serverPublicKey") ?: throw Exception("Server public key is required")
          try {
              println("Parsing server public key: $publicKey")
              peerBuilder.parsePublicKey(publicKey)
              println("Server public key parsed successfully")
          } catch (e: ParseException) {
              println("Failed to parse server public key: ${e.message}")
              throw Exception("Invalid server public key format: ${e.message}, Key: $publicKey")
          }

          // Parse preshared key if provided
          if (config.hasKey("presharedKey")) {
              val presharedKey = config.getString("presharedKey")
              try {
                  println("Parsing preshared key: $presharedKey")
                  presharedKey?.let { keyString ->
                      val key = Key.fromBase64(keyString)
                      peerBuilder.setPreSharedKey(key)
                  }
                  println("Preshared key parsed successfully")
              } catch (e: Exception) {
                  println("Failed to parse preshared key: ${e.message}")
                  throw Exception("Invalid preshared key format: ${e.message}, Key: $presharedKey")
              }
          }

          // Parse endpoint
          val serverAddress = config.getString("serverAddress") ?: throw Exception("Server address is required")
          val serverPort = if (config.hasKey("serverPort")) config.getInt("serverPort") else throw Exception("Server port is required")
          if (serverPort < 1 || serverPort > 65535) {
              throw Exception("Port must be between 1 and 65535, got: $serverPort")
          }
          val endpoint = "$serverAddress:$serverPort"
          try {
              println("Parsing endpoint: $endpoint")
              peerBuilder.parseEndpoint(endpoint)
              println("Endpoint parsed successfully")
          } catch (e: ParseException) {
              println("Failed to parse endpoint: ${e.message}")
              throw Exception("Invalid endpoint format: ${e.message}, Endpoint: $endpoint")
          }

          // Parse allowed IPs (optional but recommended)
          val allowedIPsList = mutableListOf<String>()
          if (config.hasKey("allowedIPs")) {
              val allowedIPsArray = config.getArray("allowedIPs")
              if (allowedIPsArray != null) {
                  for (i in 0 until allowedIPsArray.size()) {
                      val maybeIp = if (!allowedIPsArray.isNull(i)) allowedIPsArray.getString(i) else null
                      maybeIp?.let { allowedIPsList.add(it) }
                  }
              }
          }

          // If no allowed IPs provided, default to 0.0.0.0/0 (route everything) — optional choice
          if (allowedIPsList.isEmpty()) {
              allowedIPsList.add("0.0.0.0/0")
          }

          // Add allowed IPs to peer
          try {
              println("Adding allowed IPs to peer: $allowedIPsList")
              for (ipString in allowedIPsList) {
                  peerBuilder.addAllowedIp(InetNetwork.parse(ipString))
              }
              println("Allowed IPs added to peer successfully")
          } catch (e: ParseException) {
              println("Failed to add allowed IPs to peer: ${e.message}")
              throw Exception("Invalid peer allowedIP format: ${e.message}, IPs: $allowedIPsList")
          }

          println("Building WireGuard config...")
          val configBuilder = Config.Builder()
          configBuilder.setInterface(interfaceBuilder.build())
          configBuilder.addPeer(peerBuilder.build())

          this.config = configBuilder.build()
          println("WireGuard config built successfully")

          this.tunnel = object : Tunnel {
              override fun getName(): String = "WireGuardTunnel"
              override fun onStateChange(newState: Tunnel.State) {
                  println("WireGuard tunnel state changed to: $newState")
              }
          }

          try {
              println("Checking backend and tunnel state...")
              println("Backend initialized: $backend")
              println("Tunnel initialized: $tunnel")
              println("Config ready: $config")

              println("Attempting to set tunnel state to UP...")
              if (backend == null) {
                  throw Exception("Backend is null. Call initialize() before connect().")
              }
              if (tunnel == null) {
                  throw Exception("Tunnel is null.")
              }
              if (this.config == null) {
                  throw Exception("Config is null. Call connect() with valid config.")
              }
              val intent = VpnService.prepare(reactApplicationContext)
              if (intent != null) {
                  throw Exception("VPN permission not granted. Call requestVpnPermission() before connect().")
              }

              backend?.setState(tunnel!!, Tunnel.State.UP, this.config!!)
              println("Successfully set tunnel state to UP")
              promise.resolve(null)
          } catch (e: Exception) {
              println("Failed to set tunnel state: ${e.message}")
              println("Exception stack trace:")
              e.printStackTrace()
              println("Backend state: ${backend != null}")
              println("Tunnel state: ${tunnel != null}")
              println("Config state: ${this.config != null}")
              throw Exception("Failed to set tunnel state: ${e.message}")
          }
      } catch (e: Exception) {
          println("Connection failed with error: ${e.message}")
          println("Exception stack trace:")
          e.printStackTrace()
          promise.reject("CONNECT_ERROR", "Failed to connect: ${e.message}")
      }
  }

  override fun disconnect(promise: Promise) {
      try {
          if (tunnel != null && backend != null && config != null) {
              backend?.setState(tunnel!!, Tunnel.State.DOWN, config!!)
              promise.resolve(null)
          } else {
              promise.reject("DISCONNECT_ERROR", "Tunnel not initialized")
          }
      } catch (e: Exception) {
          promise.reject("DISCONNECT_ERROR", "Failed to disconnect: ${e.message}")
      }
  }

  override fun getStatus(promise: Promise) {
      try {
          val state = if (tunnel != null && backend != null) {
              backend?.getState(tunnel!!)
          } else {
              Tunnel.State.DOWN
          }

          val status = Arguments.createMap().apply {
              putBoolean("isConnected", state == Tunnel.State.UP)
              putString("tunnelState", state?.name ?: "UNKNOWN")
          }
          promise.resolve(status)
      } catch (e: Exception) {
          val status = Arguments.createMap().apply {
              putBoolean("isConnected", false)
              putString("tunnelState", "ERROR")
              putString("error", e.message)
          }
          promise.resolve(status)
      }
  }

}
