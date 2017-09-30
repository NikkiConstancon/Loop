const webSockMessenger = require('../lib/webSockMessenger')
const patientManager = require("../patientManager");
var subscriberManager = require('../subscriberManager');

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
                channel({ PASS: "a" })
            }
        }
    }
})

