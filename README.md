# socketconnectioncapacitor

TCP/UDP socket connection plugin for Capacitor applications.

## Installation

```bash
npm install socketconnectioncapacitor
npx cap sync
```

## Requirements

- iOS 14.0+
- Android API level 23+ (Android 6.0+)
- Capacitor 6.2.0+

## Features

- TCP socket support
- UDP socket communication
- iOS and Android platform support
- Native socket implementations
- Event-based message handling

## API Documentation

<docgen-index>

* [`echo(...)`](#echo)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------

</docgen-api>

## Usage

Here is an example of how to use the `socketconnectioncapacitor` plugin in an Ionic Capacitor project:

```typescript
import { Plugins } from '@capacitor/core';
const { SocketConnectionCapacitor } = Plugins;

async function connectToServer() {
  try {
    const result = await SocketConnectionCapacitor.connect({
      host: 'example.com',
      port: 8080,
      ssl: false
    });
    console.log('Connected:', result);
  } catch (error) {
    console.error('Connection failed:', error);
  }
}

async function sendMessage(message: string) {
  try {
    const result = await SocketConnectionCapacitor.sendMessage({ message });
    console.log('Message sent:', result);
  } catch (error) {
    console.error('Failed to send message:', error);
  }
}

function listenForMessages() {
  SocketConnectionCapacitor.addListener('messageReceived', (info: any) => {
    console.log('Message received:', info.message);
  });
}

async function disconnectFromServer() {
  try {
    const result = await SocketConnectionCapacitor.disconnect();
    console.log('Disconnected:', result);
  } catch (error) {
    console.error('Disconnection failed:', error);
  }
}

// Example usage
connectToServer();
listenForMessages();
sendMessage('Hello, server!');
disconnectFromServer();
```

## Development

Run the following commands to set up for development:

```bash
npm install
npx cap sync
```

## Testing

Run the test suite with:

```bash
npm run test
```

This will run:

- iOS tests (`verify:ios`)
- Android tests (`verify:android`)
- Web tests (`verify:web`)

## Code Style

Format code using:

```bash
npm run format
```

## License

MIT

## Author

touya.r@gmail.com