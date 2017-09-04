var Device = require('zetta-device');
var util = require('util');
var extend = require('node.extend');

//var icon = 'http://i.imgur.com/R9xBixo.png';


function degToRad(x) {
  return x * ( Math.PI / 180 );
}

var Heart = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "BPM";
  this._opts = opts || {};
  this._increment = this._opts['increment'] || 15;
  this._timeOut = null;
  this._counter = 0;


  /*this.style = extend(true, this.style, {properties: {
    stateImage: {
      //url: icon,
      tintMode: 'original'
    }
  }});*/

};
util.inherits(Heart, Device);

Heart.prototype.init = function(config) {
  config
    .type('Heart')
    .name('Heart-rate')


    .monitor('vitals');

  this._startMockData();
};


Heart.prototype._startMockData = function(cb) {
  var self = this;
  this._timeOut = setInterval(function() {
    self.vitals = 60 + (Math.sin(degToRad(self._counter)) + 1.0) * 10;
    self._counter += self._increment;
  }, 400);
}

Heart.prototype._stopMockData = function(cb) {
  clearTimeout(this._timeOut);
}
