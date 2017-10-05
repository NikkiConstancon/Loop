/**
 * @fileOverview
 * The entry point for the node program, where the Zetta server is initialized to allow Zettalets
 * a connection. This file also starts up the Webserver to allow the android application to make requests.
 *
 * @arg --test is a stdin argument that will activate the testing suit intertwined within  most modules
 * @arg --test-keepAlive is a stdin argument that will prevent the server from killing itestlf after execution
 * @arg --test-drop is a stdin argument that will cause the database to be automatically dropped after execution
 **/
require('./services/LoadServices')

var zetta = require('zetta');
var url = require('url');

require('./webServer')
var sharedKeys = require('../Shared/sharedKeys')
var Hook = require('./lib/zettaHook')

var logger = require('./revaLog')
logger.level = 'error'

var dbManager1 = require("./userManager");
var patientManager = require("./patientManager");
var patientDataManager = require("./patientDataManager");

const realtimeDataService = require('./services/RealtimeDataService')


//init zetta as usual, but dont call link yet
//  the listen call is deferred to the hook
var initializedZetta = zetta('peers').name('Zettar')


/*//Threshold functions:
const THRESHOLDS = [
    { DeviceName: "Body_temperature", min: ,max: },
    { DeviceName: "Body_insulin", min: ,max: },
    { DeviceName: "Heart-rate", min: ,max: },
    { DeviceName: "Body_glucose", min: ,max: },
    { DeviceName: , min: ,max: },
    { DeviceName: , min: ,max: },
]
var prevThresholds = 
function checkTreshold(){
    
    
}
*/
//END


//pass the initialized zetta var to a new hook
var hook = new Hook(initializedZetta)
    //call listen as you wold on zetta
    .listen(3009, function (e) {
        if (e) {
            console.log("Zetta error:", e)
        } else {
            console.log('Zettar is running');
        }
    })
    .registerStreamListener({
        topicName: 'vitals',
        cb: function (info, response) {
//            console.log(this.result.state);
//            console.log(response.data);
            realtimeDataService.publish(info, response);
            patientDataManager.addInstance({ PatientUsername: info.from, DeviceID: info.name, TimeStamp: parseFloat(response.timestamp), Value: parseFloat(response.data) });
        },
        errcb: function (e) {
            console.log(e)
        },
        connect: function (peer) {
            realtimeDataService.connectZettalet(peer.name)
        },
        disconnect: function (peer) {
            realtimeDataService.disconnectZettalet(peer.name)
            patientManager.getPatient({ Username: peer.name }).then(function (pat) {
                pat.disconnectAllDevices();
            })
        },
        deviceConnect: function (info) {
            realtimeDataService.connectDevice(info.from, info)
            patientManager.getPatient({ Username: info.from }).then(function (pat) {
                pat.connectDevice(info.name);
            })
        }
    })

