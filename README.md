# 🛡️ rn-wireguard-tunnel

[![npm version](https://img.shields.io/npm/v/rn-wireguard-tunnel.svg)](https://www.npmjs.com/package/rn-wireguard-tunnel)
[![npm downloads](https://img.shields.io/npm/dm/rn-wireguard-tunnel.svg)](https://www.npmjs.com/package/rn-wireguard-tunnel)
[![license](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![platform](https://img.shields.io/badge/platform-React%20Native-blue.svg)](https://reactnative.dev/)

A **React Native TurboModule** that lets you build custom VPN apps using **WireGuard**, powered by a **native Kotlin backend** built on the official **WireGuard Go** library.  
It enables **direct JavaScript control** of VPN initialization, permission handling, key generation, connection, and disconnection — all within React Native.

---

## 📦 Installation

```bash
npm install rn-wireguard-tunnel
# or
yarn add rn-wireguard-tunnel
```

---

## ⚙️ Android Setup

You **do not need to manually add permissions or services** for this library —  
the package already declares the required permissions and `VpnService` in its manifest.

However, ⚠️ **do not declare another `<service>`** with  
`android.permission.BIND_VPN_SERVICE` in your app’s own `AndroidManifest.xml`.  
Having multiple VPN services may cause permission conflicts or runtime errors.

---

## 🔄 Recommended Flow

Here’s the proper usage flow to ensure everything initializes correctly:

1. **Generate & store keys securely** (e.g., using `AsyncStorage` or SecureStore)
2. **Initialize** the WireGuard backend
3. **Request VPN permission**
4. **Connect** using your configuration
5. **Check connection status**
6. **Disconnect** when done

---

## 🧠 Example Usage

```tsx
import React, { useState } from 'react';
import { Button, View, Text } from 'react-native';
import {
  initialize,
  requestVpnPermission,
  connect,
  disconnect,
  getStatus,
  generateKeys,
} from 'rn-wireguard-tunnel';

export default function App() {
  const [status, setStatus] = useState('');

  const startVpn = async () => {
    try {
      // 1️⃣ Generate WireGuard keys (store these securely for production)
      const keys = await generateKeys();
      console.log('Generated keys:', keys);

      // 2️⃣ Initialize backend
      await initialize();

      // 3️⃣ Request VPN permission
      const granted = await requestVpnPermission();
      if (!granted) {
        alert('VPN permission denied');
        return;
      }

      // 4️⃣ Connect using your VPN config
      await connect({
        clientPrivateKey: keys.privateKey,
        clientAddress: '10.0.0.2/32',
        serverPublicKey: '<YOUR_SERVER_PUBLIC_KEY>',
        serverAddress: '<SERVER_IP>',
        serverPort: 51820,
        allowedIPs: ['0.0.0.0/0'],
        dns: ['1.1.1.1'],
      });

      // 5️⃣ Get connection status
      const current = await getStatus();
      setStatus(JSON.stringify(current, null, 2));
    } catch (e) {
      console.error('VPN Error:', e);
    }
  };

  const stopVpn = async () => {
    await disconnect();
    const current = await getStatus();
    setStatus(JSON.stringify(current, null, 2));
  };

  return (
    <View style={{ padding: 20 }}>
      <Button title="Start VPN" onPress={startVpn} />
      <Button title="Stop VPN" onPress={stopVpn} />
      <Text>Status: {status}</Text>
    </View>
  );
}
```

---

## 🧩 API Reference

### 🔑 `generateKeys()`

Generates a **WireGuard private/public key pair**.

```ts
const { privateKey, publicKey } = await generateKeys();
```

---

### ⚙️ `initialize()`

Initializes the WireGuard backend. Must be called before connecting.

```ts
await initialize();
```

---

### 🔐 `requestVpnPermission()`

Requests Android VPN permission from the user.

```ts
await requestVpnPermission();
```

---

### 🌐 `connect(config)`

Connects to a WireGuard VPN using the provided configuration.

```ts
await connect({
  clientPrivateKey: '...',
  clientAddress: '10.0.0.2/32',
  serverPublicKey: '...',
  serverAddress: 'vpn.example.com',
  serverPort: 51820,
  allowedIPs: ['0.0.0.0/0'],
  dns: ['1.1.1.1'],
});
```

---

### 🔌 `disconnect()`

Stops the active WireGuard tunnel.

```ts
await disconnect();
```

---

### 📊 `getStatus()`

Returns the current VPN tunnel status.

```ts
const status = await getStatus();
/*
{
  isConnected: boolean,
  tunnelState: 'ACTIVE' | 'INACTIVE' | 'ERROR',
  error?: string
}
*/
```

---

## 📘 Type Definitions

```ts
interface WireGuardConfig {
  clientPrivateKey: string;
  clientAddress: string; // e.g. 10.0.0.2/32
  serverPublicKey: string;
  serverAddress: string;
  serverPort: number;
  allowedIPs: string[];
  dns?: string[];
  mtu?: number;
  presharedKey?: string;
}

interface WireGuardStatus {
  isConnected: boolean;
  tunnelState: 'ACTIVE' | 'INACTIVE' | 'ERROR';
  error?: string;
}
```

---

## 🧱 Requirements

| Platform     | Support                              |
| ------------ | ------------------------------------ |
| Android      | ✅ Full (Kotlin + WireGuard backend) |
| iOS          | 🚧 Coming soon                       |
| React Native | 0.72+                                |
| Node         | 18+                                  |

---

## 🧑‍💻 Author

**Abhinav Verma**  
[GitHub](https://github.com/Abhinav-1v) • [npm](https://www.npmjs.com/package/rn-wireguard-tunnel)

---

## 💡 Contributing

PRs are welcome!  
If you’d like to improve documentation, fix issues, or add iOS support — feel free to open a pull request.

---

## 📄 License

MIT © Abhinav Verma

---

## ⭐ Support

If you find this package useful, please consider starring ⭐ [the repo on GitHub](https://github.com/Abhinav-1v/rn-wireguard-tunnel).  
It helps more developers discover it and keeps development active.
