/* eslint-disable @typescript-eslint/no-unused-vars */

import { useEffect, useState } from 'react';
import { Text, View, StyleSheet, Button, Alert } from 'react-native';
import {
  connect,
  generateKeys,
  getStatus,
  initialize,
  requestVpnPermission,
} from 'rn-wireguard-tunnel';
import type { WireGuardConfig } from '../../src/NativeRnWireguardTunnel';

export default function App() {
  const [keys, setKeys] = useState({
    privateKey: 'GENERATE AND STORE YOUR OWN PRIVATE KEY',
    publicKey: 'GENERATE AND STORE YOUR OWN PUBLIC KEY',
  });

  const [vpnConnected, setVpnConnected] = useState(false);
  const config: WireGuardConfig = {
    clientPrivateKey: keys.privateKey,
    clientAddress: '10.0.0.2/32',
    serverPublicKey: 'SERVER PUBLIC KEY HERE __',
    serverAddress: 'ADD SERVER ADDRESS HERE __',
    serverPort: 51820,
    allowedIPs: ['0.0.0.0/0'],
  };
  const [vpnGranted, setVpnGranted] = useState(false);
  return (
    <View style={styles.container}>
      <View style={styles.container}>
        <Button
          title="Request VPN Permission"
          onPress={async () => {
            const granted = await requestVpnPermission();
            setVpnGranted(granted);
          }}
        />
      </View>
      <View style={styles.container}>
        <Text>VPN GRANTED: {vpnGranted.toString()}</Text>
      </View>
      <View style={styles.container}>
        <Button
          title="Connect VPN"
          onPress={async () => {
            try {
              if (!vpnGranted) {
                Alert.alert('VPN Permission not granted');
                return;
              }
              await initialize();
              await connect(config);
              const status = await getStatus();
              console.log('VPN Status after connect:', status);
              setVpnConnected(status.isConnected);
            } catch (e) {
              console.error('Error connecting VPN:', e);
            }
          }}
        />
      </View>
      <View style={styles.container}>
        <Text>VPN CONNECTED: {vpnConnected.toString()}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'white',
    color: 'black',
  },
});
