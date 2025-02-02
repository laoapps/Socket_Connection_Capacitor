import { SocketConnectionCapacitor } from 'socketconnectioncapacitor';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    SocketConnectionCapacitor.echo({ value: inputValue })
}
