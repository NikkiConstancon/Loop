

var Device = require('zetta').Device;
var util = require('util');

var ScaleVisceralFat = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "%";
  this.toMonitor = "SCAVF";
}

util.inherits(ScaleVisceralFat, Device);

ScaleVisceralFat.prototype.init = function(config) 
{
    config
      .type('bluetooth_device')
      .name('scale_visceral_fat')
      .monitor('vitals');
}

ScaleVisceralFat.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
