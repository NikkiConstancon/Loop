var Device = require('zetta-device');
var util = require('util');
var extend = require('node.extend');

var icon = 'https://emojipedia-us.s3.amazonaws.com/thumbs/120/twitter/103/candy_1f36c.png';

const MAX = 38;
const MIN = 5;

function degToRad(x) {
  return x * ( Math.PI / 180 );
}

var Glucose = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "mg/dL";
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
    .name('Body Glucose')


    .monitor('vitals')
    
    .when('safe', {allow: ['isLow','isHigh']})
    .when('low', {allow: ['isSafe','isHigh']})
    .when('high', {allow: ['isLow','isSafe']})
    
    .map('isLow', this.isLow)
    .map('isHigh', this.isHigh)
    .map('isSafe', this.isSafe)

  this._startMockData();
};

Glucose.prototype.isLow = function(cb) {
    this.state = 'low';
    cb();
}

Glucose.prototype.isHigh = function(cb) {
    this.state = 'high';
    cb();
}

Glucose.prototype.isSafe = function(cb) {
    this.state = 'safe';
    cb();
}
Glucose.prototype._startMockData = function(cb) {
  var self = this;
  this._timeOut = setInterval(function() {
    self.vitals = 37 + (Math.sin(degToRad(self._counter)) + 1.0) * 1;
    self._counter += self._increment;
        if(self.vitals > MAX){
        self.isHigh(function(){});
    } else if(self.vitals < MIN){ 
        self.isLow(function(){});
    } else{
        self.isSafe(function(){});
    }
    
  }, 700);
}

Glucose.prototype._stopMockData = function(cb) {
  clearTimeout(this._timeOut);
}
