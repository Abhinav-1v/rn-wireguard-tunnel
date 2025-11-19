# 🛡️ rn-wireguard-tunnel

[![npm version](https://img.shields.io/npm/v/rn-wireguard-tunnel.svg)](https://www.npmjs.com/package/rn-wireguard-tunnel)
[![npm downloads](https://img.shields.io/npm/dm/rn-wireguard-tunnel.svg)](https://www.npmjs.com/package/rn-wireguard-tunnel)
[![license](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![platform](https://img.shields.io/badge/platform-React%20Native-blue.svg)](https://reactnative.dev/)

A **React Native TurboModule** that lets you build custom VPN apps using **WireGuard** — directly from JavaScript.  
Implements native Android support via **Kotlin** & the **WireGuard Go backend**.

---

## 📦 Installation

```bash
npm install rn-wireguard-tunnel
# or
yarn add rn-wireguard-tunnel
```

---

## ⚙️ Android Setup

Add the following permissions and service entry to your app’s `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.BIND_VPN_SERVICE" />

<application>
  <service
      android:name="com.rnwireguardtunnel.WireguardVpnService"
      android:permission="android.permission.BIND_VPN_SERVICE"
      android:exported="true">
      <intent-filter>
          <action android:name="android.net.VpnService" />
      </intent-filter>
  </service>
</application>
```

> ⚠️ If your app already has a VPN service, ensure only one `<service>` entry exists.

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
      await initialize();
      const granted = await requestVpnPermission();
      if (!granted) {
        alert('VPN permission denied');
        return;
      }

      // Generate keys (for testing/demo)
      const keys = await generateKeys();
      console.log('Keys:', keys);

      await connect({
        clientPrivateKey: keys.privateKey,
        clientAddress: '10.0.0.2/32',
        serverPublicKey: '<YOUR_SERVER_PUBLIC_KEY>',
        serverAddress: '<SERVER_IP>',
        serverPort: 51820,
        allowedIPs: ['0.0.0.0/0'],
        dns: ['1.1.1.1'],
      });

      const current = await getStatus();
      setStatus(JSON.stringify(current));
    } catch (e) {
      console.error('VPN Error:', e);
    }
  };

  const stopVpn = async () => {
    await disconnect();
    const current = await getStatus();
    setStatus(JSON.stringify(current));
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

Initializes the WireGuard backend.

```ts
await initialize();
```

---

### 🔐 `requestVpnPermission()`

Requests Android VPN permission (must be called before connecting).

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
  serverAddress: 'your.server.com',
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

Returns the current tunnel status.

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
It helps more developers discover it!
