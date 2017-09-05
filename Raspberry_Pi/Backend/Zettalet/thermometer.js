var Device = require('zetta-device');
var util = require('util');
var extend = require('node.extend');

var icon = 'http://i.imgur.com/R9xBixo.png';


function degToRad(x) {
  return x * ( Math.PI / 180 );
}

var Thermometer = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "°C";
  this._opts = opts || {};
  this._increment = this._opts['increment'] || 15;
  this._timeOut = null;
  this._counter = 0;


  this.style = extend(true, this.style, {properties: {
    stateImage: {
      url: icon,
      tintMode: 'original'
    }
  }});

};
util.inherits(Thermometer, Device);

Thermometer.prototype.init = function(config) {
  config
    .type('thermometer')
    .name('Body_temperature')


    .monitor('vitals');

  this._startMockData();
};


Thermometer.prototype._startMockData = function(cb) {
  var self = this;
  this._timeOut = setInterval(function() {
    self.vitals = 37 + (Math.sin(degToRad(self._counter)) + 1.0) * 1;
    self._counter += self._increment;
  }, 300);
}

Thermometer.prototype._stopMockData = function(cb) {
  clearTimeout(this._timeOut);
}
