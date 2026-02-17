import type { HybridObject } from 'react-native-nitro-modules';

type NitroAdvertisingIdResult = string;

export interface NitroAdvertisingId
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  requestPermission(): Promise<NitroAdvertisingIdResult>;
  getAdvertisingId(): string;
}
