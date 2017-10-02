

var Device = require('zetta').Device;
var util = require('util');

var ECGPulse = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "bpm";
  this.toMonitor = "ECGPU";
}

util.inherits(ECGPulse, Device);

ECGPulse.prototype.init = function(config) 
{
    config
      .type('wired_device')
      .name('electrocardiograph_pulse_monitor')
      .monitor('vitals');
}

ECGPulse.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
