# 🛡️ rn-wireguard-tunnel

[![npm version](https://img.shields.io/npm/v/rn-wireguard-tunnel.svg)](https://www.npmjs.com/package/rn-wireguard-tunnel)
[![npm downloads](https://img.shields.io/npm/dm/rn-wireguard-tunnel.svg)](https://www.npmjs.com/package/rn-wireguard-tunnel)
[![license](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![platform](https://img.shields.io/badge/platform-React%20Native-blue.svg)](https://reactnative.dev/)

A **React Native TurboModule** that enables building custom VPN apps using **WireGuard**, backed by a native **Kotlin + WireGuard-Go** integration.  
You get full JS control over VPN initialization, permission handling, key generation, connection & disconnection — all inside React Native.

---

## 📦 Installation

```bash
npm install rn-wireguard-tunnel
# or
yarn add rn-wireguard-tunnel
```

---

## ⚙️ Android Setup

You **do not need to manually add permissions or services**.  
The package already includes the correct `VpnService` declaration.

### ⚠️ Important

**Do NOT add another `<service>` with `android.permission.BIND_VPN_SERVICE` in your app.**  
Multiple VPN services will cause permission conflicts and runtime crashes.

---

## 🔄 Recommended Flow

1. Generate & store WireGuard keys
2. Initialize backend
3. Request VPN permission
4. Connect using configuration
5. Get VPN status
6. Disconnect

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
    const keys = await generateKeys();
    await initialize();

    const granted = await requestVpnPermission();
    if (!granted) return alert('VPN permission denied');

    await connect({
      clientPrivateKey: keys.privateKey,
      clientAddress: '10.0.0.2/32',
      serverPublicKey: '<SERVER_PUBLIC_KEY>',
      serverAddress: '<SERVER_IP>',
      serverPort: 51820,
      allowedIPs: ['0.0.0.0/0'],
      dns: ['1.1.1.1'],
    });

    const current = await getStatus();
    setStatus(JSON.stringify(current, null, 2));
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

Generates a WireGuard private/public key pair.

```ts
const { privateKey, publicKey } = await generateKeys();
```

---

### ⚙️ `initialize()`

Initializes the native WireGuard backend.

```ts
await initialize();
```

---

### 🔐 `requestVpnPermission()`

Requests Android VPN permission.

```ts
await requestVpnPermission();
```

---

### 🌐 `connect(config)`

Connect to the WireGuard tunnel.

```ts
await connect(config);
```

---

### 🔌 `disconnect()`

Stop the tunnel.

```ts
await disconnect();
```

---

### 📊 `getStatus()`

Get the current status.

```ts
const s = await getStatus();
```

---

## 📘 Type Definitions

```ts
export interface WireGuardConfig {
  clientPrivateKey: string;
  clientAddress: string; // eg. 10.0.0.2/32
  serverPublicKey: string;
  serverAddress: string;
  serverPort: number; // eg. 51820
  allowedIPs: string[]; // default: ['0.0.0.0/0']
  dns?: string[];
  mtu?: number;
  presharedKey?: string;
}

export interface WireGuardStatus {
  isConnected: boolean;
  tunnelState: 'ACTIVE' | 'INACTIVE' | 'ERROR';
  error?: string;
}
```

---

## 📘 Type Usage

### Importing Types

```ts
import type { WireGuardConfig, WireGuardStatus } from 'rn-wireguard-tunnel';
```

### Using Config

```ts
const config: WireGuardConfig = {
  clientPrivateKey: '<KEY>',
  clientAddress: '10.0.0.2/32',
  serverPublicKey: '<SERVER_PUBLIC_KEY>',
  serverAddress: 'vpn.example.com',
  serverPort: 51820,
  allowedIPs: ['0.0.0.0/0'],
  dns: ['1.1.1.1'],
};
```

### Handling Status

```ts
const status: WireGuardStatus = await getStatus();
```

---

## 🧱 Requirements

| Platform     | Support        |
| ------------ | -------------- |
| Android      | ✅ Full        |
| iOS          | 🚧 Coming Soon |
| React Native | 0.72+          |
| Node         | 18+            |

---

## 🧑‍💻 Author

**Abhinav Verma**  
GitHub: https://github.com/Abhinav-1v  
npm: https://www.npmjs.com/package/rn-wireguard-tunnel

---

## 💡 Contributing

PRs welcome!

---

## 📄 License

MIT © Abhinav Verma
