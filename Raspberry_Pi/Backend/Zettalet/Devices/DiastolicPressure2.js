var Device = require('zetta-device') ;
var util = require('util');
var extend = require('node.extend');
function degToRad(x) {
  return x * ( Math.PI / 180 );
}
//var icon = 'https://emojipedia-us.s3.amazonaws.com/thumbs/120/twitter/103/candy_1f36c.png';

var DiastolicPressure = module.exports = function(opts) {
  Device.call(this);
  this.vitals = 0;
  this.units = "BPM";
  this._opts = opts || {};
  this._increment = this._opts['increment'] || 15;
  this._timeOut = null;
  this._counter = 0;

 /*  this.style = extend(true, this.style, {properties: {
    stateImage: {
      url: icon,
      tintMode: 'original'
    }
}});*/
}

util.inherits(DiastolicPressure, Device);

DiastolicPressure.prototype.init = function(config) {
  // Set up the state machine
    config
    .type('Heart')
    .name('Heart-rate')
      //.stream('diastolic_pressure', this.streamDiastolicPressure);
      .monitor('vitals');
  console.log("in bloodpressure constructor");
  //this.streamDiastolicPressure();
  this.controller = null;
  //this._startMockData();
}

DiastolicPressure.prototype.setController = function(controller)
{
  console.log("setting controller now: " + controller);
  this.controller = controller;
  console.log("\tsetting controller now: " + this.controller);
}

DiastolicPressure.prototype.streamDiastolicPressure = function() {
  
    var self = this;
  console.log("in stream meain: controller is " + this.controller);
  if (self.controller != null)
  {
    var controller = self.controller;
      controller.onChange(function(){
      console.log("====openDiastollic====");
        
        if (controller.toMonitor == "DYSP")
        {
        //console.log('\tToMonitor:', controller.toMonitor);
        console.log('\tDiastolic data:', controller.data);
        self.vitals = 12;
      }
      console.log("====closeDiastolic====");
      });
    };
}

DiastolicPressure.prototype._startMockData = function(cb) {
    var self = this;
  this._timeOut = setInterval(function() {
    self.vitals = 60 + (Math.sin(degToRad(self._counter)) + 1.0) * 10;
    self._counter += self._increment;
  }, 400);
  
  this._timeOut = setInterval(function() {
      console.log("in stream meain: controller is " + self.controller);
  if (self.controller != null)
  {
    var controller = self.controller;
    /*
      controller.onChange(function(){
      console.log("====openDiastollic====");
        
        if (controller.toMonitor == "DYSP")
        {
        //console.log('\tToMonitor:', controller.toMonitor);
        console.log('\tDiastolic data:', controller.data);
        self.vitals = controller.data;
      }
      console.log("====closeDiastolic====");
      });
    */
    self.vitals = parseInt(controller.data);
    };
  }, 1000);
}

DiastolicPressure.prototype._stopMockData = function(cb) {
  clearTimeout(this._timeOut);
}

DiastolicPressure.prototype.print = function() {
  console.log("printing diastolic_pressure");
}
