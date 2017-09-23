var Device = require('zetta-device');
var util = require('util');
var extend = require('node.extend');

var icon = 'https://emojipedia-us.s3.amazonaws.com/thumbs/120/twitter/103/syringe_1f489.png';


function degToRad(x) {
  return x * ( Math.PI / 180 );
}

var Insulin = module.exports = function(opts) {
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
util.inherits(Insulin, Device);

Insulin.prototype.init = function(config) {
  config
    .type('insulin')
    .name('Body_insulin')


    .monitor('vitals');

  this._startMockData();
};


Insulin.prototype._startMockData = function(cb) {
  var self = this;
  this._timeOut = setInterval(function() {
    self.vitals = 37 + (Math.sin(degToRad(self._counter)) + 1.0) * 1;
    self._counter += self._increment;
  }, 1000);
}

Insulin.prototype._stopMockData = function(cb) {
  clearTimeout(this._timeOut);
}