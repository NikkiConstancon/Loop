var Device = require('zetta-device');
var util = require('util');
var extend = require('node.extend');

var icon = 'https://emojipedia-us.s3.amazonaws.com/thumbs/120/twitter/103/candy_1f36c.png';


function degToRad(x) {
  return x * ( Math.PI / 180 );
}

var Glucose = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "BPM";
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
util.inherits(Glucose, Device);

Glucose.prototype.init = function(config) {
  config
    .type('glucose')
    .name('Body_glucose')


    .monitor('vitals');

  this._startMockData();
};


Glucose.prototype._startMockData = function(cb) {
  var self = this;
  this._timeOut = setInterval(function() {
    self.vitals = 37 + (Math.sin(degToRad(self._counter)) + 1.0) * 1;
    self._counter += self._increment;
  }, 700);
}

Glucose.prototype._stopMockData = function(cb) {
  clearTimeout(this._timeOut);
}
