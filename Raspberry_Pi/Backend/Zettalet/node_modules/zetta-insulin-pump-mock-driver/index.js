var Device = require('zetta-device');
var util = require('util');
var extend = require('node.extend');

var ANIMAS_VIBE_IMG = 'https://www.animascorp.co.uk/images/cgm-trend-screen-blueprint.jpg';

var TIMEOUT = 3000;
function degToRad(x) {
  return x * ( Math.PI / 180 );
}

var InsulinPump = module.exports = function(opts) {
  Device.call(this);
  this.opts = opts || {};
  this.concentration = 0;
  this.batteryLevel = .50;
  this._increment = this.opts['increment'] || 15;
  this.serialNumber = 'A345';
  this.currentVersion = 1.0;
  this._timeOut = null;
  this._counter = 0;
  
  this.style = extend(true, this.style, {properties: {
    stateImage: {
      url: ANIMAS_VIBE_IMG,
      tintMode: 'original'
    },
    concentration: {
      display: 'billboard',
      significantDigits: 1,
      symbol: 'mg/dl'
    }
  }});
  
};
util.inherits(InsulinPump, Device);

InsulinPump.prototype.init = function(config) {
  var name = this.opts.name || "Brian's Insulin Pump";

  config
    .name(name)
    .type('insulin-pump')
    .state('ready')
    .when('ready', {allow: ['make-not-ready', 'update-firmware']})
    .when('not-ready', {allow: ['make-ready', 'update-firmware']})
    .when('updating-firmware', {allow: []})
    .map('make-not-ready', this.makeNotReady)
    .map('make-ready', this.makeReady)
    .map('update-firmware', this.updateFirmware, [
      { type: 'text', name: 'url' }
    ])
    .monitor('concentration')
    .monitor('batteryLevel');

  this._startMockData();
};

InsulinPump.prototype.updateFirmware = function(url, cb) {
  this.state = 'updating-firmware';
  this._stopMockData();
  cb();

  console.log('updating firmware with URL: ' + url);
  // call ftp http get
  // send over USB or Bluetooth

  var self = this;
  setTimeout(function(){
    self.state = 'ready';
    self._startMockData(cb);
    cb();
  }, 3000);

}

InsulinPump.prototype.makeReady = function(cb) {
  this.state = 'ready';
  this._startMockData();
  cb();
}

InsulinPump.prototype.makeNotReady = function(cb) {
  this.state = 'not-ready'
  this._stopMockData();
  cb();
}

InsulinPump.prototype._startMockData = function(cb) {
  var self = this;
  this._timeOut = setInterval(function() {
    self.concentration = 292 * (Math.sin(degToRad(self._counter)) + 1.0) + 19;
    if (self.concentration < 20) {
      self.concentration = 'LO';
    } else if (self.concentration > 600) {
      self.concentration = 'HI';
    }
    self._counter += self._increment;
  }, 1000);
}

InsulinPump.prototype._stopMockData = function(cb) {
  clearTimeout(this._timeOut);
}
