

var Device = require('zetta').Device;
var util = require('util');

var Electromygraph = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "V";
  this.toMonitor = "EMGSG";
}

util.inherits(Electromygraph, Device);

Electromygraph.prototype.init = function(config) 
{
    config
      .type('wired_device')
      .name('electromygraph_signal')
      .monitor('vitals');
}

Electromygraph.prototype.setVitals = function(toMonitor, newVitals)
{
  if (toMonitor == this.toMonitor && !isNaN(newVitals))    
    this.vitals = newVitals;
}
