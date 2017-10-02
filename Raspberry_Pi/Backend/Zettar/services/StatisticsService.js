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
            GRAPH_INFO: function (transmitter, msg, key, channel) {
                console.log("Stats Request sent")
                var obj = {Username: msg.Username, DeviceId: msg.DeviceId, StartTime: msg.StartTime, EndTime: msg.EndTime}
                console.log(obj);
                dataManager.getGraphPoints({Username: msg.Username, DeviceId: msg.DeviceId, StartTime: msg.StartTime, EndTime: msg.EndTime}).then(function(result){
                    console.log(result)
                    channel(result)
                }).catch(function () {
                    logger.error('GraphRetievalError', e)
                })  
            }
        }
    }
})

