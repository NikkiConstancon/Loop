var Scout = require('zetta').Scout;
var util = require('util');


var Insulin = require('./insulin');
var Thermometer = require('./thermometer');
var Glucose = require('./glucose');
var Heartbeat = require('./heart');

//Construct the scout class
StateMachineScout = module.exports = function() {
  Scout.call(this);
}
util.inherits(StateMachineScout, Scout);

StateMachineScout.prototype.init = function(next) {
  var self = this;
  setTimeout(function() {
    //self.discover(Insulin)
   // self.discover(Thermometer)
    self.discover(Glucose)
   // self.discover(Heartbeat)
  }, 1000);
  next();
}
