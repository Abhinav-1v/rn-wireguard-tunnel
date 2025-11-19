import { TurboModuleRegistry, type TurboModule } from 'react-native';

export interface WireGuardConfig {
  clientPrivateKey: string;
  clientAddress: string; // eg. 10.0.0.2/32
  serverPublicKey: string;
  serverAddress: string;
  serverPort: number; // eg. 51820
  allowedIPs: string[]; // default '0.0.0.0/0 (route everything)
  dns?: string[];
  mtu?: number;
  presharedKey?: string;
}

export interface WireGuardStatus {
  isConnected: boolean;
  tunnelState: 'ACTIVE' | 'INACTIVE' | 'ERROR';
  error?: string;
}

export interface Spec extends TurboModule {
  initialize(): Promise<void>;
  connect(config: WireGuardConfig): Promise<void>;
  disconnect(): Promise<void>;
  getStatus(): Promise<WireGuardStatus>;
  requestVpnPermission(): Promise<boolean>;
  generateKeys(): Promise<{ privateKey: string; publicKey: string }>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('RnWireguardTunnel');
