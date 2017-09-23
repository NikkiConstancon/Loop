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
    this.realTimePublisher = new RealTimePublisher(this)
    this.realTimeCollectorMap = {}//use map to only send newest data when collecting
    this.flagRealTimeTimeoutSet = false
}
RelatimeHopper.prototype.informDeviceConnect = function (info) {
    var meta = this.meta.getMeta() || {}
    meta[info.name] = { type: info.type, topicName: info.topicName }
    this.meta.setMeta(meta)
}
RelatimeHopper.prototype.informDisonnect = function () {
    this.meta.free()
    this.realTimePublisher.free()
}
RelatimeHopper.prototype.pushRealTime = function (info, response) {   
    //TODO !!! average by some function instesd of overwriting old value  !!!
    this.realTimeCollectorMap[info.name] = response.data.toPrecision(3)

    if (!this.flagRealTimeTimeoutSet) {
        this.flagRealTimeTimeoutSet = true
        setTimeout(() => {
            this.realTimePublisher.push({ [this.name]: this.realTimeCollectorMap })
            this.realTimeCollectorMap = {}
            this.flagRealTimeTimeoutSet = false
        }, 750)
    }

}

function RealTimePublisher(relatimeHopper) {
    this.relatimeHopper = relatimeHopper
    this.subscriberList = []
    patientManager.bindSubscriberListInofHook({ Username: this.relatimeHopper.name },(argObj) => {
        this.subscriberList = argObj
    })
}
RealTimePublisher.prototype.free = function () {
    patientManager.unbindSubscriberListInofHook({ Username: this.relatimeHopper.name })
}
RealTimePublisher.prototype.push = function (msgObj) {
    var userSocketContextMap = webSockMessenger.getUserSocketContextMap()
    for (var i in this.subscriberList) {
        var userUid = this.subscriberList[i]
        for (var context in userSocketContextMap[userUid]) {
            var usc = userSocketContextMap[userUid][context]
            var service = usc.subServiceMap[serviceName]
            service.publish(msgObj)
        }
    }
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
        relatimeHopperMap[info.from] && relatimeHopperMap[info.from].pushRealTime(info, response)
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
        }).catch(function () {
            return patientManager.getPatient({ Username: publisher.context.userUid }).then(function (pat) {
                //TODO: get list of patients this patient is subscriber to and regester them on the publisher
                publisher.registerMeta(publisher.context.userUid)
            })
        })

        publisher.publish(count++, function (err) {
        })
    },
    close: function (context) {
    },
    sub: function (publisher, obj) {
        publisher.publish(obj)
    }
})

