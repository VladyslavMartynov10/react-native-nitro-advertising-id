import type { HybridObject } from 'react-native-nitro-modules';

export type NitroAdvertisingIdResult =
  | 'authorized'
  | 'denied'
  | 'restricted'
  | 'notDetermined'
  | 'granted'
  | 'undetermined'
  | 'unknown';

export interface NitroAdvertisingId
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  requestPermission(): Promise<NitroAdvertisingIdResult>;
  getAdvertisingId(): string;
}
