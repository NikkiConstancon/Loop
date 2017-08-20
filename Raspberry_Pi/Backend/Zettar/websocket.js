const WebSocket = require('ws');

const wss = new WebSocket.Server({ port: 8080 });

wss.on('connection', function connection(ws) {
    ws.on('message', function incoming(message) {
        console.log('received: %s', message);
    });

    ws.send('something');
});







/*

const ws2 = new WebSocket('ws://127.0.0.1:8080');

ws2.on('open', function open() {
    const array = new Float32Array(5);

    for (var i = 0; i < array.length; ++i) {
        array[i] = i / 2;
    }

    ws2.send(array);
});

*/