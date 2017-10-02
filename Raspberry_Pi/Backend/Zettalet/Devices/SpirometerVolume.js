

var Device = require('zetta').Device;
var util = require('util');

var SpirometerVolume = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "L";
  this.toMonitor = "SPIVL";
}

util.inherits(SpirometerVolume, Device);

SpirometerVolume.prototype.init = function(config) 
{
    config
      .type('wired_device')
      .name('spirometer_air_volume')
      .monitor('vitals');
}

SpirometerVolume.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
