

var Device = require('zetta').Device;
var util = require('util');

var Pulse = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "ppm";
  this.toMonitor = "PULSE";
}

util.inherits(Pulse, Device);

Pulse.prototype.init = function(config) 
{
    config
      .type('wired_device')
      .name('pulse_monitor')
      .monitor('vitals');
}

Pulse.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
