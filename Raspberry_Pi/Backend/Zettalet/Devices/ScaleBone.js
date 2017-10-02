

var Device = require('zetta').Device;
var util = require('util');

var ScaleBone = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "%";
  this.toMonitor = "SCALB";
}

util.inherits(ScaleBone, Device);

ScaleBone.prototype.init = function(config) 
{
    config
      .type('bluetooth_device')
      .name('scale_bone')
      .monitor('vitals');
}

ScaleBone.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
