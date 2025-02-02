export interface SocketConnectionCapacitorPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
