import { EnvConfig } from "./env-config";
import { EnvType } from "./env-type";

export function isProd(config: EnvConfig): boolean {
  return config.environmentType === EnvType.production;
}

export function isSandbox(config: EnvConfig): boolean {
  return config.environmentType === EnvType.sandbox;
}

export function mbToBytes(mb: number): number {
  return mb * 1024 * 1024;
}
