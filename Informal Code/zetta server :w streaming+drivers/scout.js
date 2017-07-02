var Scout = require('zetta').Scout;
var util = require('util');
var heart_monitor = require('./heart_monitor.js');
var temp_monitor = require('./temp_monitor.js');

//Construct the scout class
StateMachineScout = module.exports = function() {
  Scout.call(this);
}
util.inherits(StateMachineScout, Scout);

StateMachineScout.prototype.init = function(next) {
  var self = this;
  setTimeout(function() {
    self.discover(heart_monitor)
    self.discover(temp_monitor)
  }, 1000);
  next();
}
