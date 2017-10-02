

var Device = require('zetta').Device;
var util = require('util');

var ScaleMuscleMass = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "%";
  this.toMonitor = "SCAMM";
}

util.inherits(ScaleMuscleMass, Device);

ScaleMuscleMass.prototype.init = function(config) 
{
    config
      .type('bluetooth_device')
      .name('scale_MuscleMass')
      .monitor('vitals');
}

ScaleMuscleMass.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
