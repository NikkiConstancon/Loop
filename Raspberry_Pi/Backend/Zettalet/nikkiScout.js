var Scout = require('zetta').Scout;
var util = require('util');
var controller = require('./Devices/ArduinoController.js');
var arduinoController = new controller();

//device library:
var deviceLibrary = [];

//Blood Pressure Monitor
	deviceLibrary.push(require('./Devices/SystolicPressure.js'));
	deviceLibrary.push(require('./Devices/DiastolicPressure.js'));
	deviceLibrary.push(require('./Devices/BPPulse.js'));

//Pulsioximeter
/*
	deviceLibrary.push(require('./Devices/Pulse.js'));
	deviceLibrary.push(require('./Devices/OxygenInBlood.js'));

//Electrocardiograph
	deviceLibrary.push(require('./Devices/Electrocardiograph.js'));
	deviceLibrary.push(require('./Devices/ECGPulse.js'));

//Electromygraphy
	deviceLibrary.push(require('./Devices/Electromyograph.js'));
	deviceLibrary.push(require('./Devices/MuscleRate.js'));

//Spirometer	 	
	deviceLibrary.push(require('./Devices/SpirometerVolume.js'));
	deviceLibrary.push(require('./Devices/SpirometerAirFlow.js'));

//Scale
	deviceLibrary.push(require('./Devices/ScaleWeight.js'));
	deviceLibrary.push(require('./Devices/ScaleBone.js'));
	deviceLibrary.push(require('./Devices/ScaleBodyFat.js'));
	deviceLibrary.push(require('./Devices/ScaleMuscleMass.js'));
	deviceLibrary.push(require('./Devices/ScaleBodyWater.js'));
	deviceLibrary.push(require('./Devices/ScaleVisceralFat.js'));
	deviceLibrary.push(require('./Devices/ScaleCalories.js'));

//Mock Devices
	var heart = require('./Devices/heart.js');
	deviceLibrary.push(require('./Devices/heart.js'));
	var DiastolicPressure2 = require('./Devices/DiastolicPressure2.js');
*/

//Construct the scout class
DeviceController = module.exports = function() {
  Scout.call(this);
}
util.inherits(DeviceController, Scout);

//Creating the scout
DeviceController.prototype.init = function(next) {
	var self = this;
		
	setTimeout(function(){
		var tempDevice;
		for (var i = 0; i < deviceLibrary.length; i++) {
			console.log("discovering device: ");
			tempDevice = self.discover(deviceLibrary[i]);
			if (tempDevice != null)
				arduinoController.addDevice(tempDevice);
		}

		setInterval(function() {
			arduinoController.connect();
		}, 1000);
		arduinoController.onChange(function(){
			console.log("just got data");
		});
	},1000);	
  	next();
}
