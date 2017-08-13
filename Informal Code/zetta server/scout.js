var Scout = require('zetta').Scout;
var util = require('util');
// var heart_monitor = require('./test.js');
// var temp_monitor = require('./temp_monitor.js');
var tempp = require('./thermometer.js')
// var insulin = require('zetta-glucose-meter-mock-driver');
var car = require('zetta-automobile-mock-driver');


//Construct the scout class
StateMachineScout = module.exports = function() {
  Scout.call(this);
}
util.inherits(StateMachineScout, Scout);

StateMachineScout.prototype.init = function(next) {
  var self = this;
  setTimeout(function() {
    // self.discover(heart_monitor)
    // self.discover(temp_monitor)
    self.discover(tempp)
    // self.discover(insulin)
    // self.discover(car)
  }, 1000);
  next();
}
