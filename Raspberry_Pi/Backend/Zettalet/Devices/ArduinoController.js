var SerialPort = require('serialport');
const Readline = SerialPort.parsers.Readline;
const parser = new Readline();
const portName = '/dev/ttyACM0';



var ArduinoController = module.exports = function() 
{	
	this.data = 0;	
	this.toMonitor = 0;
	this.devices = [];
	this.connected = false;

	this.port = new SerialPort(portName, {
			baudRate: 9600, autoOpen: false
		});	
	this.port.pipe(parser);	
}

ArduinoController.prototype.connect = function()
{
	var self = this;
	if (!self.port.isOpen)
	{
		self.port.open(function(err){
			console.log("Error: Could not connect. Plug in arduino to USB port: " + portName);
			self.connected = false;
		});
	}
	else if (self.connected == false)
	{			
		self.connected = true;
		console.log("Successfully connected to: " + portName);	
	}
}

ArduinoController.prototype.addDevice = function(newDevice)
{
	this.devices.push(newDevice);
}

ArduinoController.prototype.onChange = function(cb)
{
	var self = this;
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
		}
		cb();
	});	
}



