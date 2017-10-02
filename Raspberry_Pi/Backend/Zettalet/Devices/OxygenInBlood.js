

var Device = require('zetta').Device;
var util = require('util');

var OxygenInBlood = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "%";
  this.toMonitor = "OXYO2";
}

util.inherits(OxygenInBlood, Device);

OxygenInBlood.prototype.init = function(config) 
{
    config
      .type('wired_device')
      .name('blood_oxygen_monitor')
      .monitor('vitals');
}

OxygenInBlood.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
