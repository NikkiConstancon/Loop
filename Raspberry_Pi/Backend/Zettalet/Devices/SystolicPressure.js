var Device = require('zetta').Device;
var util = require('util');

var SystolicPressure = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "BPM";
  this.toMonitor = "SYSPR";
}

util.inherits(SystolicPressure, Device);

SystolicPressure.prototype.init = function(config) 
{
    config
      .type('wired_device')
      .name('systolic_pressure_monitor')
      .monitor('vitals');
}

SystolicPressure.prototype.setVitals = function(toMonitor, newVitals)
{
  console.log("in setvitals1: " + toMonitor);
  console.log("in setvitals2: " + this.toMonitor);
  console.log("in setvitals3: " + (this.toMonitor == toMonitor));
  console.log("in setvitals4: " + (!isNaN(newVitals)));
  console.log("in setvitals5: " + (newVitals));
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
