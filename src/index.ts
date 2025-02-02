import { registerPlugin } from '@capacitor/core';

import type { SocketConnectionCapacitorPlugin } from './definitions';

const SocketConnectionCapacitor = registerPlugin<SocketConnectionCapacitorPlugin>('SocketConnectionCapacitor', {
  web: () => import('./web').then((m) => new m.SocketConnectionCapacitorWeb()),
});

export * from './definitions';
export { SocketConnectionCapacitor };
