var Scout = require('zetta').Scout;
var util = require('util');
var heart_monitor = require('./heart_monitor.js');
var temp_monitor = require('./temp_monitor.js');


var Insulin = require('zetta-insulin-pump-mock-driver');
var Thermometer = require('zetta-thermometer-mock-driver');
var Glucose = require('zetta-glucose-meter-mock-driver');
var Heartbeat = require('zetta-mock-heartbeat-sensor');

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
    self.discover(Insulin)
    self.discover(Thermometer)
    self.discover(Glucose)
    self.discover(Heartbeat)
  }, 1000);
  next();
}
