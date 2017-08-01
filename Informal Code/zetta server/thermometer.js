var Device = require('zetta-device');
var util = require('util');

function degToRad(x) {
  return x * ( Math.PI / 180 );
}

var Thermometer = module.exports = function(opts) {
  Device.call(this);
  this.temperature = 0;
  this._opts = opts || {};
  this._increment = this._opts['increment'] || 15;
  this._timeOut = null;
  this._counter = 0;
};
util.inherits(Thermometer, Device);

Thermometer.prototype.init = function(config) {
  config
    .name('Thermometer')
    .type('thermometer')
    
    .monitor('temperature');

  this._startMockData();
};

Thermometer.prototype.makeReady = function(cb) {
  this.state = 'ready';
  this._startMockData();
  cb();
}

Thermometer.prototype.makeNotReady = function(cb) {
  this.state = 'not-ready'
  this._stopMockData();
  cb();
}

Thermometer.prototype._startMockData = function(cb) {
  var self = this;
  this._timeOut = setInterval(function() {
    self.temperature = (Math.sin(degToRad(self._counter)) + 1.0) * 50 - 10;
    self._counter += self._increment;
  }, 100);
}

Thermometer.prototype._stopMockData = function(cb) {
  clearTimeout(this._timeOut);
}
