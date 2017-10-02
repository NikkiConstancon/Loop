

var Device = require('zetta').Device;
var util = require('util');

var ScaleWeight = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "kg";
  this.toMonitor = "SCALW";
}

util.inherits(ScaleWeight, Device);

ScaleWeight.prototype.init = function(config) 
{
    config
      .type('bluetooth_device')
      .name('scale_weight')
      .monitor('vitals');
}

ScaleWeight.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
