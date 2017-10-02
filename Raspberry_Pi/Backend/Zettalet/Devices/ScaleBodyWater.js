

var Device = require('zetta').Device;
var util = require('util');

var ScaleBodyWater = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "%";
  this.toMonitor = "SCABW";
}

util.inherits(ScaleBodyWater, Device);

ScaleBodyWater.prototype.init = function(config) 
{
    config
      .type('bluetooth_device')
      .name('scale_body_water')
      .monitor('vitals');
}

ScaleBodyWater.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
