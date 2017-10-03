

var Device = require('zetta').Device;
var util = require('util');

var ScaleWeight = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "kg";
  this.toMonitor = "SCALW";
}

util.inherits(ScaleWeight, Device);

ScaleWeight.prototype.init = function(config) 
{
    config
      .type('bluetooth_device')
      .name('scale_weight')
      .monitor('vitals');
}

ScaleWeight.prototype.setVitals = function(toMonitor, newVitals)
{
	console.log("in weight setvitals toMonitor is: " + toMonitor);
	console.log("in weight setvitals newVitals is: " + newVitals);
	console.log("in weight setvitals newVitals is NAN?: " + !isNaN(newVitals));
	if (toMonitor == this.toMonitor && !isNaN(newVitals))  
	{  
	    this.vitals = newVitals;
		console.log("device data (weight) changed to: " + this.vitals);
	}
}
