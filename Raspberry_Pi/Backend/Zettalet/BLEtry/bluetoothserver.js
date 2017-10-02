var btSerial = new (require('bluetooth-serial-port')).BluetoothSerialPort();
 var address = "88-1B-99-09-60-B5"
btSerial.on('found', function(address, name) {
    btSerial.findSerialPortChannel(address, function(channel) {
        btSerial.connect(address, channel, function() {
            console.log('connected');
 
			for (var i = 0; i < 8; i++)
            btSerial.write(new Buffer('12', 'utf-8'), function(err, bytesWritten) {
                if (err) console.log(err);
            });
 
            btSerial.on('data', function(buffer) {
                console.log(buffer.toString('utf-8'));
            });
        }, function () {
            console.log('cannot connect');
        });
 
        // close the connection when you're ready
        btSerial.close();
    }, function() {
        console.log('found nothing');
    });
});
 
btSerial.inquire();
