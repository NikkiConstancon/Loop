/**
 * @fileOverview
 * The entry point for the node program, where the Zetta server is initialized to allow Zettalets
 * a connection. This file also starts up the Webserver to allow the android application to make requests.
 *
 * @arg --test is a stdin argument that will activate the testing suit intertwined within  most modules
 * @arg --test-keepAlive is a stdin argument that will prevent the server from killing itestlf after execution
 * @arg --test-drop is a stdin argument that will cause the database to be automatically dropped after execution
 **/


var zetta = require('zetta');
var url = require('url');

require('./webServer')
var sharedKeys = require('../Shared/sharedKeys')
var Hook = require('./lib/zettaHook')

var logger = require('./revaLog')

var dbManager1 = require("./userManager");
var patientManager = require("./patientManager");
var patientDataManager = require("./patientDataManager");

const realtimeDataService = require('./lib/realtimeDataService')


//init zetta as usual, but dont call link yet
//  the listen call is deferred to the hook
var initializedZetta = zetta('peers').name('Zettar')

var callback = function (info, data) {

    patientDataManager.addInstance({PatientUsername : info.from, DeviceID : data[0].topic, TimeStamp : data[0].timestamp, Value : parseFloat(data[0].data)  });
    info
    // patientDataManager.addInstance({PatientUsername : info.from, DeviceID : data[0].topic, TimeStamp : data[0].timestamp, Value : parseFloat(data[0].data)  });
//console.log(data[0].topic)
//console.log(info.from)
}

//pass the initialized zetta var to a new hook
var hook = new Hook(initializedZetta)
    //call listen as you wold on zetta
    .listen(3009, function (e) {
        if (e) {
            console.log("Zetta errot:", e)
        } else {
            console.log('Zettar is running');
        }
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
        //temporary for testing with old stuff
        topicName: 'value',
        cb: function (info, response) {
            realtimeDataService.publish(info, response);
        },
        errcb: function (e) {
            console.log(e)
        }
    })
    .registerStreamListener({
        topicName: 'vitals',
        cb: function (info, response) {
            realtimeDataService.publish(info, response);
        },
        errcb: function (e) {
            console.log(e)
        }
    })
    /*.registerStreamListener({
        topicName: 'vitals',
        where: { type: 'Heart', name: 'Heart-rate' },
        cb: callback,
        errcb: function (e) {
            console.log(e)
        }
    })
    .registerStreamListener({
        topicName: 'vitals',
        where: { type: 'thermometer', name: 'Body_temperature' },
        cb: callback,
        errcb: function (e) {
            console.log(e)
        }
    })
    .registerStreamListener({
        topicName: 'vitals',
        where: { type: 'glucose' },
        cb: callback,
        errcb: function (e) {
            console.log(e)
        }
    })
    .registerStreamListener({
        topicName: 'vitals',
        where: { type: 'insulin' },
        cb: callback,
        errcb: function (e) {
            console.log(e)
        }
    })*/
