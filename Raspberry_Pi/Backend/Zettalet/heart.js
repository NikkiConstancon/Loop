var Device = require('zetta-device');
var util = require('util');
var extend = require('node.extend');

//var icon = 'http://i.imgur.com/R9xBixo.png';

const MAX = 38;
const MIN = 5;

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
    .name('Heart Rate')


    .monitor('vitals')
    .when('safe', {allow: ['isLow','isHigh']})
    .when('low', {allow: ['isSafe','isHigh']})
    .when('high', {allow: ['isLow','isSafe']})
    
    .map('isLow', this.isLow)
    .map('isHigh', this.isHigh)
    .map('isSafe', this.isSafe)

  this._startMockData();
};

Heart.prototype.isLow = function(cb) {
    this.state = 'low';
    cb();
}

Heart.prototype.isHigh = function(cb) {
    this.state = 'high';
    cb();
}

Heart.prototype.isSafe = function(cb) {
    this.state = 'safe';
    cb();
}

Heart.prototype._startMockData = function(cb) {
  var self = this;
  this._timeOut = setInterval(function() {
    self.vitals = 60 + (Math.sin(degToRad(self._counter)) + 1.0) * 10;
    self._counter += self._increment;
        if(self.vitals > MAX){
        self.isHigh(function(){});
    } else if(self.vitals < MIN){ 
        self.isLow(function(){});
    } else{
        self.isSafe(function(){});
    }
    
  }, 400);
}

Heart.prototype._stopMockData = function(cb) {
  clearTimeout(this._timeOut);
}
