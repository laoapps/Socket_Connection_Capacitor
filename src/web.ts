import { WebPlugin } from '@capacitor/core';

import type { SocketConnectionCapacitorPlugin } from './definitions';

export class SocketConnectionCapacitorWeb extends WebPlugin implements SocketConnectionCapacitorPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
