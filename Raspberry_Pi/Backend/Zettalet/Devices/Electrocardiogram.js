var SerialPort = require('serialport');
const Readline = SerialPort.parsers.Readline;

var Device = require('zetta').Device;
var util = require('util');
const parser = new Readline();

// Making a serial Port
var port = new SerialPort('/dev/ttyACM0', {
  baudRate: 19200
});

// Making Device
var Electrocardiogram = module.exports = function() {
  Device.call(this);
}

util.inherits(Electrocardiogram, Device);

Electrocardiogram.prototype.init = function(config) {
  // Set up the state machine
    config
      .type('wired_device')
      //.state('off') needed?
      .name('electrocardiogram')
      .stream('ECGvolt', this.streamECGvolt);
}

port.pipe(parser);
Electrocardiogram.prototype.streamECGvolt = function(stream) {

	
	parser.on('data', function (data) {
	  console.log('Data:', data);
	});
}

