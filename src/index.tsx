import RnWireguardTunnel from './NativeRnWireguardTunnel';
import type {
  WireGuardConfig,
  WireGuardStatus,
} from './NativeRnWireguardTunnel';

export async function initialize(): Promise<void> {
  return RnWireguardTunnel.initialize();
}

export function connect(config: WireGuardConfig): Promise<void> {
  return RnWireguardTunnel.connect(config);
}

export function disconnect(): Promise<void> {
  return RnWireguardTunnel.disconnect();
}

export function getStatus(): Promise<WireGuardStatus> {
  return RnWireguardTunnel.getStatus();
}

export function requestVpnPermission(): Promise<boolean> {
  return RnWireguardTunnel.requestVpnPermission();
}

export function generateKeys(): Promise<{
  privateKey: string;
  publicKey: string;
}> {
  return RnWireguardTunnel.generateKeys();
}

export type { WireGuardConfig, WireGuardStatus };
