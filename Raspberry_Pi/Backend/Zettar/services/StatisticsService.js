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
                patientManager.getPatient({ Username: msg }).then(function (pat) {
                    channel(pat.DeviceMap)
                })
            }
        },
        GRAPH_POINTS: {
            RAW: function (transmitter, msg, key, channel) {
                console.log("Stats Request sent" + msg.nameValuePairs)
             
               console.log("Stats Request sent" + msg.nameValuePairs.Username)
                var tmp = msg.nameValuePairs
                var obj = {Username: tmp.Username, DeviceId: tmp.DeviceId, StartTime: tmp.StartTime, EndTime: tmp.EndTime}
                console.log(obj);
                dataManager.getGraphPoints(obj).then(function(result){
                    console.log("Graph points: ")
                    console.log(result)
                    channel(result)
                }).catch(function () {
                    logger.error('GraphRetievalError', e)
                })  
            }
        }
    }
})

