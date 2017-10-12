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
                }
                minMaxAvg = endResult[device][key];
                    
            }
            if(count == 0){
                avgX = (midEnd + midStart ) / 2
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
//                console.log(params);
                patientManager.getDeviceMap({ Username: tmp.Username }).then(function (pat) { 
                    dataManager.getGraphPoints(params).then(function(result){
                        if(result == false){
                            channel(false);
                            
                        }else{
                            //console.log(result);
                            var endResult = {};
                            var minMax = {};
                            for(var device in result){
                                var id = result[device].device;
                                if(tmp.DeviceID)
                                    id = tmp.DeviceID
                                 if(! endResult[id])
                                    endResult[id] = [];
                                if(!minMax[id])
                                    minMax[id] = [];
                                if(result[device])
                                    minMax[id].push({Min: Number.POSITIVE_INFINITY, Max: Number.NEGATIVE_INFINITY, Avg: 0, count: 0});
                                    
                                    if(minMax[id][0].Min > result[device].y){
                                        minMax[id][0].Min = result[device].y
                                    }
                                    if(minMax[id][0].Max < result[device].y){
                                        minMax[id][0].Max = result[device].y
                                    }
                                    minMax[id][0].Avg += result[device].y
                                    minMax[id][0].count ++;
                                    endResult[id].push({x: result[device].x, y: result[device].y});    
                            }
                            var average;
                            for(var device in minMax){
                                average =  minMax[device][0].Avg/ minMax[device][0].count
                                endResult[device].push({Min: minMax[device][0].Min, Max: minMax[device][0].Max, Avg: average});
                                
                            }
                        endResult = compress(tmp.StartTime, tmp.EndTime, endResult);
  //                      console.log("\n\n");
    //                        console.log(endResult);
                            channel(endResult);
                        }
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

