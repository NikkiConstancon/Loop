

var Device = require('zetta').Device;
var util = require('util');

var MuscleRate = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "cpm";
  this.toMonitor = "EMGMR";
}

util.inherits(MuscleRate, Device);

MuscleRate.prototype.init = function(config) 
{
    config
      .type('wired_device')
      .name('muscle_rate_monitor')
      .monitor('vitals');
}

MuscleRate.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
