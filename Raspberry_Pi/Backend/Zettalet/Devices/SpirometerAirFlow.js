

var Device = require('zetta').Device;
var util = require('util');

var SpirometerAirFlow = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "l/min";
  this.toMonitor = "SPIAF";
}

util.inherits(SpirometerAirFlow, Device);

SpirometerAirFlow.prototype.init = function(config) 
{
    config
      .type('wired_device')
      .name('spirometer_air_flow')
      .monitor('vitals');
}

SpirometerAirFlow.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
