const webSockMessenger = require('./webSockMessenger')
const patientManager = require("../patientManager");
var subscriberManager = require('../subscriberManager');

const logger = require('../revaLog')

const serviceName = 'RTDS'

var fromQueueMap = {}
var relatimeHopperMap = {}
//one per zettalet
function RelatimeHopper(zettaletName) {
    this.name = zettaletName
    this.meta = metaHandler.newMeta(zettaletName)
}
RelatimeHopper.prototype.informDeviceConnect = function (info) {
    var meta = this.meta.getMeta() || {}
    meta[info.name] = { type: info.type, topicName: info.topicName }
    this.meta.setMeta(meta)
}
RelatimeHopper.prototype.informDisonnect = function () {
    this.meta.free()
}


function pushData(msg) {
    var userSocketContextMap = webSockMessenger.getUserSocketContextMap()
    for (var user in userSocketContextMap) {
        for (var context in userSocketContextMap[user]) {
            var usc = userSocketContextMap[user][context]
            var service = usc.subServiceMap[serviceName]
            service.publish(msg)
        }
    }
}

var sevice = module.exports = {
    publish: function (info, response) {
        if (!fromQueueMap[info.from]) {
            fromQueueMap[info.from] = []
            setTimeout(function () {
                pushData({ [info.from]: fromQueueMap[info.from] })
                fromQueueMap[info.from] = null
            }, 8)
        }
        fromQueueMap[info.from].push({ [info.name]: response.data.toPrecision(3) })
    },
    connectHopper: function (zettaletName) {
        relatimeHopperMap[zettaletName] = new RelatimeHopper(zettaletName)
    },
    disconnectHopper: function (zettaletName) {
        try {
            relatimeHopperMap[zettaletName].informDisonnect()
            delete relatimeHopperMap[zettaletName]
        } catch (e){
            logger.error(e)
        }
    },
    informDeviceConnect: function (zettaletName, info) {
        relatimeHopperMap[zettaletName].informDeviceConnect(info)
    }
}

var metaHandler = webSockMessenger.attach(serviceName, {
    connect: function (publisher) {
        var count = 0;

        subscriberManager.getsubscriber({ Email: publisher.context.userUid }).then(function (sub) {
            var userUids = sub.PatientList
            for (var i in userUids) {
                publisher.registerMeta(userUids[i])
            }
        }).catch((e) => { logger.debug(e)})

        publisher.publish(count++, function (err) {
        })
    },
    close: function (context) {
    },
    sub: function (publisher, obj) {
        publisher.publish(obj)
    }
})

