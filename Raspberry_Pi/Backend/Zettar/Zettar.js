
var zetta = require('zetta');
var url = require('url');
// var display = require('./display.js');

require('./webServer')
var sharedKeys = require('../Shared/sharedKeys')
var Hook = require('./lib/zettaHook')

var logger = require('./revaLog')

var dbManager1 = require("./userManager");
var patientManager = require("./patientManager");
var patientDataManager = require("./patientDataManager");


//init zetta as usual, but dont call link yet
//  the listen call is deferred to the hook
var initializedZetta = zetta('peers').name('Zettar')

var callback = function(info, data){
   // patientDataManager.addInstance({PatientUsername : info.from, DeviceID : data[0].topic, TimeStamp : data[0].timestamp, Value : parseFloat(data[0].data)  });

}

//pass the initialized zetta var to a new hook
var hook = new Hook(initializedZetta)
    //call listen as you wold on zetta
    .listen(3009, function () {
        console.log('Zettar is running : 3009');
    })
    //here you hook the streams
    .registerStreamListener({
        connect: function (peer) {
            patientManager.bindZettalet(peer.name, encodeURI(peer.name))
                .then(function (pat) {
                    //do it this way, else field is out of date
                    patientManager.getPatient(pat).then(function (pat) {
                        logger.info('user [' + pat.Username + "]'s device connected with api uri: " + pat.ZettaletUuid)
                    })
                }).catch(function (err) {
                    logger.warn('no user bound for device with uuid ' + peer.name)
                    //peer.ws.close()
                })
        },
        disconnect: function (peer) {
            patientManager.bindZettalet(peer.name, '-')
                .then(function (pat) {
                    //do it this way, else field is out of date
                    patientManager.getPatient(pat).then(function (pat) {
                        logger.info('user [' + pat.Username + "]'s device disconnected and api uri set to: " + pat.ZettaletUuid)
                    })
                })
        }
    })
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
  