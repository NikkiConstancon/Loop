

var Device = require('zetta').Device;
var util = require('util');

var Electrocardiograph = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "V";
  this.toMonitor = "ECGSG";
}

util.inherits(Electrocardiograph, Device);

Electrocardiograph.prototype.init = function(config) 
{
    config
      .type('wired_device')
      .name('electrocardiogram_signal')
      .monitor('vitals');
}

Electrocardiograph.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
