var Scout = require('zetta').Scout;
var util = require('util');
var controller = require('./Devices/ArduinoController.js');
var arduinoController = new controller();
//var BleController = require('./Devices/BLEController.js');
//var bleController = new BleController();

//device library:
var deviceLibrary = [];
//Blood Pressure Monitor
/*
	var SystolicPressure = require('./Devices/SystolicPressure.js');
	deviceLibrary.push(require('./Devices/SystolicPressure.js'));
	var DiastolicPressure = require('./Devices/DiastolicPressure.js');
	deviceLibrary.push(require('./Devices/DiastolicPressure.js'));
	var BPPulse = require('./Devices/BPPulse.js');
	deviceLibrary.push(require('./Devices/BPPulse.js'));
*/
//Pulsioximeter

	var Pulse = require('./Devices/Pulse.js');
	deviceLibrary.push(require('./Devices/Pulse.js'));
	var OxygenInBlood = require('./Devices/OxygenInBlood.js');
	deviceLibrary.push(require('./Devices/OxygenInBlood.js'));
/*
//Electrocardiograph
	var Electrocardiograph = require('./Devices/Electrocardiograph.js');
	deviceLibrary.push(require('./Devices/Electrocardiograph.js'));
//	var ECGPulse = require('./Devices/ECGPulse.js');
//	deviceLibrary.push(require('./Devices/ECGPulse.js'));
/*
//Electromygraphy
	var Electromyograph = require('./Devices/Electromyograph.js');
	deviceLibrary.push(require('./Devices/Electromyograph.js'));
	var MuscleRate = require('./Devices/MuscleRate.js');
	deviceLibrary.push(require('./Devices/MuscleRate.js'));
//Spirometer
	 	
	var SpirometerVolume = require('./Devices/SpirometerVolume.js');
	deviceLibrary.push(require('./Devices/SpirometerVolume.js'));
	var SpirometerAirFlow = require('./Devices/SpirometerAirFlow.js');
	deviceLibrary.push(require('./Devices/SpirometerAirFlow.js'));
/*
//Scale

	var scaleWeight = require('./Devices/ScaleWeight.js');
	deviceLibrary.push(require('./Devices/ScaleWeight.js'));
	console.log("required library");
	var scaleBone = require('./Devices/ScaleBone.js');
	deviceLibrary.push(require('./Devices/ScaleBone.js'));
	console.log("required library");
	var scaleBodyFat = require('./Devices/ScaleBodyFat.js');
	deviceLibrary.push(require('./Devices/ScaleBodyFat.js'));
	console.log("required library");
	var scaleMuscleMass = require('./Devices/ScaleMuscleMass.js');
	deviceLibrary.push(require('./Devices/ScaleMuscleMass.js'));
	console.log("required library");
	var scaleBodyWater = require('./Devices/ScaleBodyWater.js');
	deviceLibrary.push(require('./Devices/ScaleBodyWater.js'));
	console.log("required library");
	var scaleVisceralFat = require('./Devices/ScaleVisceralFat.js');
	deviceLibrary.push(require('./Devices/ScaleVisceralFat.js'));
	console.log("required library");
	var scaleCalories = require('./Devices/ScaleCalories.js');
	deviceLibrary.push(require('./Devices/ScaleCalories.js'));
	console.log("required library");
/*
//Mock Devices
*/
//	var heart = require('./Devices/heart.js');
//	deviceLibrary.push(require('./Devices/heart.js'));
//	var DiastolicPressure2 = require('./Devices/DiastolicPressure2.js');

//Construct the scout class
DeviceController = module.exports = function() {
  Scout.call(this);
console.log("in rinus scout");
}
util.inherits(DeviceController, Scout);

DeviceController.prototype.init = function(next) {

		var self = this;
console.log("in rinus scout");


			console.log("before settimeout: ");
setTimeout(function(){
		var tempDevice;
		for (var i = 0; i < deviceLibrary.length; i++) 
		{
			console.log("discovering device: ");
			tempDevice = self.discover(deviceLibrary[i]);
			if (tempDevice != null)
				arduinoController.addDevice(tempDevice);
		}

	  	arduinoController.onChange(function(){console.log("just got data");});




/////scale stuffs
	//tempDevice = self.discover(scaleWeight)
	//bleController.addDevice(tempDevice);
  	//bleController.onChange(function(){console.log("just got bluetooth data");});
},1000);

console.log("in rinus scout");
	//this.discover(heart);
/*



	var machine;
	var machine2;
	var machine3;
	//machine = self.discover(BloodPressureMonitor);

  //setTimeout(function() {
//	machine.setController(arduinoController);
		//self.discover(heart);
		//machine2 = self.discover(DiastolicPressure);
		//console.log("in scout before discoverer");
	//	machine3 = self.discover(SystolicPressure);
		//machine2.setController(arduinoController);
		//machine3.setController(arduinoController);
	//	arduinoController.addDevice(machine2);
	//	arduinoController.addDevice(machine3);
	//	//machine3.streamDiastolicPressure2();
	//	console.log("Machine 3: " + machine3);
    //	arduinoController.onChange(function(){console.log("got data?");});
		//machine2.print();
		//machine2._startMockData();
//	  machine.streamBloodPressure();
	  //machine2.streamDiastolicPressure();
//  }, 1000);
//    setTimeout(function() {
//	machine.setController(arduinoController);
//		self.discover(heart);
//		machine2 = self.discover(DiastolicPressure);
//		machine2.setController(arduinoController);
//	  machine.streamBloodPressure();
	  //machine2.streamDiastolicPressure();
		//machine3.streamDiastolicPressure2();
  //}, 2000);
 */ 
	
  next();
}
