const webSockMessenger = require('../lib/webSockMessenger')
const patientManager = require("../patientManager");

const subscriberManager = require('../subscriberManager');
const dataManager = require('../patientDataManager');
const logger = require('../revaLog')

const serviceName = 'Stats'

function compress(StartTime, EndTime, endResult){
        var result = {};
    for(var device in endResult){
        result[device] = [];
        var segment = (EndTime - StartTime ) / 600;
        var midEnd = StartTime;
        var midStart = StartTime;
        while(midStart < EndTime){ //for each segment that is there
            midEnd += segment;
            var avgX = 0;
            var avgY = 0;
            var count = 0;
            var minMaxAvg = {};
            for(var key in endResult[device]){
                if(endResult[device][key].x > midStart && endResult[device][key].x < midEnd){
                    count ++;
                    avgX += endResult[device][key].x;
                    avgY += endResult[device][key].y;
                }else if(endResult[device][key].Min) {
                    minMaxAvg = endResult[device][key];
                    
                }
                    
            }
            if(count == 0){
                avgX = (midEnd + midStart ) / 2
  //              console.log('pushing : ' + avgX + "  " + avgY);
//                result[device].push({x: avgX, y:0});  
            } else{
                avgX /= count;
                avgY /= count;
                result[device].push({x: avgX, y:avgY});  
                
            } 
            midStart = midEnd;
        }
        result[device].push(minMaxAvg); 
    }
    return result;
}

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
                var params
                if(!tmp.DeviceID){
                        params = {
                        Username: tmp.Username  ,
                        StartTime:  tmp.StartTime,
                        EndTime:tmp.EndTime,
                    }
                } else{
                    params = {
                        Username: tmp.Username  ,
                        DeviceID: tmp.DeviceID,
                        StartTime:  tmp.StartTime,
                        EndTime:tmp.EndTime,
                    }
                    
                }
                console.log(params);
                patientManager.getDeviceMap({ Username: tmp.Username }).then(function (pat) { 
                    dataManager.getGraphPoints(params).then(function(result){
                        var endResult = {};
                        var size = 1;
                        if(!tmp.DeviceID)
                            size = Object.keys(pat).length;
                        console.log(size);
                        for(var i= 0; i < size; i ++){
                            var id = Object.keys(pat)[i];
                            if(tmp.DeviceID)
                                id = tmp.DeviceID
                            endResult[id] = [];
                            for(var j = 0; j <  Object.keys(result).length - 1; j++){
                                endResult[id].push({x: result[Object.keys(result)[j]].x, y: result[Object.keys(result)[j]].y});
                            }
                            
                            endResult[id].push({Min: result[Object.keys(result)[j]].Min, Max: result[Object.keys(result)[j]].Max, Avg: result[Object.keys(result)[j]].Avg});
                        }
                        endResult = compress(tmp.StartTime, tmp.EndTime, endResult);
                        console.log(endResult);
                        channel(endResult);
                    }).catch(function (e) {
                        logger.error('GraphRetievalError', e)
                        channel(false)
                    }) 
                }).catch(function (e) {
                    logger.error('DeviceMapRetievalError', e)
                    channel(false)
                }) 
            }
        }
    }
})

