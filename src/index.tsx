import { NitroModules } from 'react-native-nitro-modules';
import type { NitroAdvertisingId } from './NitroAdvertisingId.nitro';

const NitroAdvertisingIdHybridObject =
  NitroModules.createHybridObject<NitroAdvertisingId>('NitroAdvertisingId');

export function multiply(a: number, b: number): number {
  return NitroAdvertisingIdHybridObject.multiply(a, b);
}
