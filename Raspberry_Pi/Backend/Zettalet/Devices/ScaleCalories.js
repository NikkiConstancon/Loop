

var Device = require('zetta').Device;
var util = require('util');

var ScaleCalories = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "Kcal";
  this.toMonitor = "SCALC";
}

util.inherits(ScaleCalories, Device);

ScaleCalories.prototype.init = function(config) 
{
    config
      .type('bluetooth_device')
      .name('scale_calories')
      .monitor('vitals');
}

ScaleCalories.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
