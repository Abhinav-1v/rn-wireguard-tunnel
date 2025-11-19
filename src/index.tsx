import RnWireguardTunnel from './NativeRnWireguardTunnel';
import type {
  WireGuardConfig,
  WireGuardStatus,
} from './NativeRnWireguardTunnel';

export async function initialize(): Promise<void> {
  return RnWireguardTunnel.initialize();
}

export async function connect(config: WireGuardConfig): Promise<void> {
  return RnWireguardTunnel.connect(config);
}

export async function disconnect(): Promise<void> {
  return RnWireguardTunnel.disconnect();
}

export async function getStatus(): Promise<WireGuardStatus> {
  return RnWireguardTunnel.getStatus();
}

export function requestVpnPermission(): Promise<boolean> {
  return RnWireguardTunnel.requestVpnPermission();
}

export async function generateKeys(): Promise<{
  privateKey: string;
  publicKey: string;
}> {
  return RnWireguardTunnel.generateKeys();
}
