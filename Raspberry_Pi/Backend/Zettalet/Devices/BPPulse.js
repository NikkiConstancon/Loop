

var Device = require('zetta').Device;
var util = require('util');

var BPPulse = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "ppm";
  this.toMonitor = "BPPUL";
}

util.inherits(BPPulse, Device);

BPPulse.prototype.init = function(config) 
{
    config
      .type('wired_device')
      .name('blood_pressure_pulse_monitor')
      .monitor('vitals');
}

BPPulse.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
