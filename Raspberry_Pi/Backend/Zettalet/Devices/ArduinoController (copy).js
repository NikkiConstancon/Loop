var SerialPort = require('serialport');
const Readline = SerialPort.parsers.Readline;
const parser = new Readline();
const portName = '/dev/ttyACM0';

var ArduinoController = module.exports = function() 
{
	
	this.data = 0;
	
	this.toMonitor = 0;
	this.devices = [];
	this.port = new SerialPort(portName, {
		baudRate: 9600
	});
	this.port.pipe(parser);
//console.log("in controller constructor");
	
	
}

ArduinoController.prototype.addDevice = function(newDevice)
{
	this.devices.push(newDevice);
}

ArduinoController.prototype.onChange = function(cb)
{
	
	var self = this;
	
//	console.log("\tin onChange: " + self.data);
	parser.on('data', function (data) {
		console.log('Data from arduino:', data);
		if (data.length > 5 && data.length < 10)
		{
			self.toMonitor = data.substr(0, 5);
			console.log('\tTo Monitor:', self.toMonitor);
			self.data = parseFloat(data.substr(5));
			console.log("\tData to Change: " + parseFloat(data.substr(5)));
			for (var i = 0; i < self.devices.length; i++) {
				self.devices[i].setVitals(self.toMonitor, self.data);
			};
			//self.devices[0].setVitals(self.toMonitor, self.data);
			//console.log("device data changed to: " + self.devices[0].vitals);
		}
		cb();
	});	
}



