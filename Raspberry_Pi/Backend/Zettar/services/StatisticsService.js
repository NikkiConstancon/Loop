const webSockMessenger = require('../lib/webSockMessenger')
const patientManager = require("../patientManager");

const subscriberManager = require('../subscriberManager');
const dataManager = require('../patientDataManager');
const logger = require('../revaLog')

const serviceName = 'Stats'


var sevice = module.exports = {
}
const publisherHandler = webSockMessenger.attach(serviceName, {
    defaultEnabled: true,
    connect: function (transmiter) {
        //publisher = publisherHandler.getPublisher(transmiter.getUserUid())
        //publisher.setMeta({nice:"adsfasdf"})
    },
    close: function (context) {
    },
    receiver: function (transmiter, obj) {
        transmiter.transmit(obj)
    },
    subListUpdater: function (pubName, next) {
        next([pubName])
    },
    channels: {
        INFO: {
            DEV_LIST: function (transmitter, msg, key, channel) {
                patientManager.getDeviceMap({ Username: msg }).then(function (pat) {
                    channel(pat);
                })
            }
        },
        GRAPH_POINTS: {
            RAW: function (transmitter, msg, key, channel) {
                var tmp = msg.nameValuePairs
                 patientManager.getDeviceMap({ Username: tmp.Username }).then(function (pat) { 
                    PatientDataManager.getGraphPoints({
                        Username: tmp.Username  ,
                        StartTime:  tmp.StartTime,
                        EndTime:tmp.EndTime,
                    }).then(function(result){
                        
                        var endResult = []
                        for(var i= 0; i < Object.keys(pat).length - 1; i ++){
                                if(pat[Object.keys(pat)[i]] == true){
                                    var subResult = [];
                                    // add device type
                                    subResult.push({deviceID: Object.keys(pat)[i]});
                                    //go through result...
                                    for(var j = 0; j <  Object.keys(result).length - 1; j++){
                                        //console.log("Device: " + result[Object.keys(result)[j]].device)
                                        //console.log("Compare: " + Object.keys(pat)[i])
                                        if(result[Object.keys(result)[j]].device == Object.keys(pat)[i]){
                                            //console.log("here: ");
                                            //console.log(result[Object.keys(result)[j]].device)
                                            subResult.push({x: result[Object.keys(result)[j]].x, y: result[Object.keys(result)[j]].y});
                                        }
                                    }
                                    endResult.push(subResult);
                                    
                                    
                                }
                        }
                        console.log(endResult);
                    }).catch(function (e) {
                        logger.error('GraphRetievalError', e)
                        channel(false)
                    }) 
                }).catch(function (e) {
                    logger.error('DeviceMapRetievalError', e)
                    channel(false)
                }) 
                
                
                
                
                
                
                
                /*//console.log("Stats Request sent" + msg.nameValuePairs)
                var tmp = msg.nameValuePairs
                patientManager.getDeviceMap({ Username: tmp.Username }).then(function (pat) {
                   
                //console.log("Stats Request sent" + msg.nameValuePairs.Username)
                    var obj = {Username: tmp.Username, DeviceId: tmp.DeviceId, StartTime: tmp.StartTime, EndTime: tmp.EndTime}
                    //console.log(obj);
                    dataManager.getGraphPoints(obj).then(function(result){
                        //console.log("Graph points: ")
                        console.log(result)
                        //channel(result)
                    }).catch(function () {
                        logger.error('GraphRetievalError', e)
                    })  
                })*/
            }
        }
    }
})

