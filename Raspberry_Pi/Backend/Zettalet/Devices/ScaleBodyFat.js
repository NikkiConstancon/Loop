

var Device = require('zetta').Device;
var util = require('util');

var ScaleBodyFat = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "%";
  this.toMonitor = "SCABF";
}

util.inherits(ScaleBodyFat, Device);

ScaleBodyFat.prototype.init = function(config) 
{
    config
      .type('bluetooth_device')
      .name('scale_body_fat')
      .monitor('vitals');
}

ScaleBodyFat.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
