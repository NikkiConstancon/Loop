var zetta = require('zetta');
var url = require('url');
// var display = require('./display.js');
var Hook = require('./lib/zettaHook')

var dbManager1 = require("./userManager");
var patientDataManager = require("./patientDataManager");


//init zetta as usual, but dont call link yet
//  the listen call is deferred to the hook
var initializedZetta = zetta('peers').name('Zettar')

var callback = function(info, data){
    patientDataManager.addInstance({PatientUsername : info.from, DeviceID : data[0].topic, TimeStamp : data[0].timestamp, Value : parseFloat(data[0].data)  });

}

//pass the initialized zetta var to a new hook
var hook = new Hook(initializedZetta)
    //call listen as you wold on zetta
    .listen(3009, function () {
        console.log('Zettar is running : 3009');
    })
    //here you hook the streams
    .registerStreamListener({
        topicName: 'value',
        where: { type: 'state_machine', name: 'heart_monitor' },
        cb: callback,
        errcb: function (e) {
            console.log(e)
        }
    })
    .registerStreamListener({
        topicName: 'value',
        where: { type: 'state_machine', name: 'temp_monitor' },
        cb: callback,
        errcb: function (e) {
            console.log(e)
        }
    })
    .registerStreamListener({
        topicName: 'concentration',
        where: { type: 'glucose-meter'},
        cb: callback,
        errcb: function (e) {
            console.log(e)
        }
    })
    .registerStreamListener({
        topicName: 'concentration',
        where: { type: 'insulin-pump'},
        cb: callback,
        errcb: function (e) {
            console.log(e)
        }
    })
    .registerStreamListener({
        topicName: 'temperature',
        where: { type: 'thermometer'},
        cb: callback,
        errcb: function (e) {
            console.log(e)
        }
    })
  