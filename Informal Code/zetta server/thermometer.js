var Device = require('zetta-device');
var util = require('util');
var extend = require('node.extend');

var icon = 'http://www.promedsupply.biz/wp-content/uploads/2015/11/OneTouch-Verio-FLEX-Glucose-Monitoring-System-1-0.jpg';


function degToRad(x) {
  return x * ( Math.PI / 180 );
}

var Thermometer = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this._opts = opts || {};
  this._increment = this._opts['increment'] || 15;
  this._timeOut = null;
  this._counter = 0;

  /*this.style = extend(true, this.style, {properties: {
    state: 'none'
    stateImage: {
      url: icon,
      tintMode: 'original'
    },
    vitals: {
      display: 'billboard',
      significantDigits: 2,
      symbol: '°C'
    }
  }});*/

};
util.inherits(Thermometer, Device);

Thermometer.prototype.init = function(config) {
  config
    .type('thermometer')
    .name('Thermometer')


    .monitor('vitals');

  this._startMockData();
};


Thermometer.prototype._startMockData = function(cb) {
  var self = this;
  this._timeOut = setInterval(function() {
    self.vitals = (Math.sin(degToRad(self._counter)) + 1.0) * 37 - 2;
    self._counter += self._increment;
  }, 100);
}

Thermometer.prototype._stopMockData = function(cb) {
  clearTimeout(this._timeOut);
}
