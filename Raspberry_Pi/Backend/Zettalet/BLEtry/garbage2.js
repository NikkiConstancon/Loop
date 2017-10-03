var btSerial = new (require('bluetooth-serial-port')).BluetoothSerialPort();
var address = "88-1B-99-09-60-B4";

function hex2a(hexx) {
    var hex = hexx.toString();//force conversion
    var str = '';
    for (var i = 0; i < hex.length; i += 2)
        str += String.fromCharCode(parseInt(hex.substr(i, 2), 16));
    return str;
}

function a2hex(str) {
  var arr = [];
  for (var i = 0, l = str.length; i < l; i ++) {
    var hex = Number(str.charCodeAt(i)).toString(16);
    arr.push(hex);
  }
  return arr.join('');
}

console.log("trying to find scale");
btSerial.on('found', function(address, name) {
    btSerial.findSerialPortChannel(address, function(channel) {
		console.log("trying to connect");
        btSerial.connect(address, channel, function() {
            console.log('connected');
 

            //// Directly after connected:
            //subscribe: send 0x01 and 0x00
            btSerial.write(new Buffer(0x01), function(err, bytesWritten) {
                if (err) console.log(err);
            });			
            btSerial.write(new Buffer(0x00), function(err, bytesWritten) {
                if (err) console.log(err);
            });			
            //send user profile:
                //  0: 0xfe
                //  0: 1
                //  0: 1
                //  0: 0
                //  0: 176
                //  0: 23
                //  0: 1
                //  0: 0x00
            btSerial.write(new Buffer(0xfe), function(err, bytesWritten) {
                if (err) console.log(err);
            });			
            btSerial.write(new Buffer(1), function(err, bytesWritten) {
                if (err) console.log(err);
            });			
            btSerial.write(new Buffer(1), function(err, bytesWritten) {
                if (err) console.log(err);
            });			
            btSerial.write(new Buffer(0), function(err, bytesWritten) {
                if (err) console.log(err);
            });			
            btSerial.write(new Buffer(176), function(err, bytesWritten) {
                if (err) console.log(err);
            });			
            btSerial.write(new Buffer(23), function(err, bytesWritten) {
                if (err) console.log(err);
            });			
            btSerial.write(new Buffer(1), function(err, bytesWritten) {
                if (err) console.log(err);
            });			
            btSerial.write(new Buffer(0x00), function(err, bytesWritten) {
                if (err) console.log(err);
            });			
            //receive data "event"
            btSerial.on('data', function(buffer) {
                console.log("got data1: " + a2hex(buffer.toString('utf-8')));
            });
            btSerial.on('data', function(buffer) {
                console.log("got data2: " + a2hex(buffer.toString('utf-8')));
            });
            btSerial.on('data', function(buffer) {
                console.log("got data3: " + a2hex(buffer.toString('utf-8')));
            });
            btSerial.on('data', function(buffer) {
                console.log("got data4: " + a2hex(buffer.toString('utf-8')));
            });

                //read one byte: 
                // if it is 0x80
                    //event[1] = read byte // payload
                    //event[2] = read byte // class
                    //event[3] = read byte // method
                //if event[0] != 0 && event[2] == 4 && event[3] == 5 //then there is user data
                    // for int i = 0; i < event[1] + 4; i++
                        //event[i] = readbyte
                                    /////note: have maxbuffer size?
                    //data processing:
                    //uint8_t scale_weight_high = MySignals_BLE.event[13];
                    // uint8_t scale_weight_low = MySignals_BLE.event[14];

                    // uint8_t scale_bodyfat_high = MySignals_BLE.event[15];
                    // uint8_t scale_bodyfat_low = MySignals_BLE.event[16];



                    // uint8_t scale_musclemass_high = MySignals_BLE.event[18];
                    // uint8_t scale_musclemass_low = MySignals_BLE.event[19];

                    // scaleData.visceralfat = MySignals_BLE.event[20];

                    // uint8_t scale_water_high = MySignals_BLE.event[21];
                    // uint8_t scale_water_low = MySignals_BLE.event[22];

                    // uint8_t scale_calories_high = MySignals_BLE.event[23];
                    // uint8_t scale_calories_low = MySignals_BLE.event[24];


                    // scaleData.weight = (scale_weight_high * 256) + scale_weight_low;
                    // scaleData.bodyfat = (scale_bodyfat_high * 256) + scale_bodyfat_low;
                    // scaleData.musclemass = (scale_musclemass_high * 256) + scale_musclemass_low;
                    // scaleData.water = (scale_water_high * 256) + scale_water_low;
                    // scaleData.calories = (scale_calories_high * 256) + scale_calories_low;

                    // scaleData.bonemass = MySignals_BLE.event[17] * 1000 / scaleData.weight;


            ////
/*			for (var i = 0; i < 8; i++)
            btSerial.write(new Buffer('12', 'utf-8'), function(err, bytesWritten) {
                if (err) console.log(err);
            });
 
            btSerial.on('data', function(buffer) {
                console.log(buffer.toString('utf-8'));
            });
*/
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
