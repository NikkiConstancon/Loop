const webSockMessenger = require('../lib/webSockMessenger')
const patientManager = require("../patientManager");

const subscriberManager = require('../subscriberManager');
const dataManager = require('../patientDataManager');
const logger = require('../revaLog')

const serviceName = 'Notifications'
const defaultSmoothFactor = 0.3 // (f * now) + (1-f)*last
//"°C"
const THRESHOLD_MAP = {
    "Body_temperature": new Threshold(0, 1000, defaultSmoothFactor, true),
    "Body_insulin": new Threshold(0, 1000, defaultSmoothFactor, true),
    "Heart-rate": new Threshold(0, 1000, defaultSmoothFactor, true),
    "Body_glucose": new Threshold(37.2, 38.9, defaultSmoothFactor, true),



    "dummy dev": new Threshold(37.2, 38.9, defaultSmoothFactor, true),
}

setInterval(function () {
    sevice.analyze({ from: "Dummy", name: "dummy dev" }, { data: 10000000000 })
}, 3000)

function Threshold(min, max, smoothFactor, flagRealtimeAnalysis){
    this.min = min
    this.max = max
    this.smoothFactor = smoothFactor
    this.flagRealtimeAnalysis = flagRealtimeAnalysis
}
Threshold.prototype.newAnalyser = function(publisher, info) {
    return new Analyser(this, publisher, info)
}

function Analyser(threshold, publisher, info) {
    this.threshold = threshold
    this.publisher = publisher
    this.info = info
    this.deviceName = info.name
    this.value = null
    this.smooth = null
    this.lastTimeNotificationSent = 0
    this.thrashGuardDelay = 0 * 30 * 1000
}



Analyser.prototype.analyze = function (now) {
    if (((new Date()).getTime() - this.lastTimeNotificationSent) > this.thrashGuardDelay) {
        if (this.threshold.flagRealtimeAnalysis) {
            if (this.value == null) {
                this.value = now
                this.smooth = now
            }
            this.updateSmooth(now)
            
            if (this.smooth < this.threshold.min) {
                this.publishThresholdDeviation(1, this.deviceName + " is below the set threshold at " + this.valueWithUnit(now))
            } else if (this.smooth > this.threshold.max) {
                this.publishThresholdDeviation(1, this.deviceName + " is above the set threshold at " + this.valueWithUnit(now))
            }
        } else {

        }
    }
}
Analyser.prototype.valueWithUnit = function(value) {
    var unit = this.threshold.unit || ""
    return value.toPrecision(3) + " " + unit
}
Analyser.prototype.updateSmooth = function (now) {
    this.value = now
    var fac = this.threshold.smoothFactor
    this.smooth = fac * now + (1 - fac) * this.smooth
}
Analyser.prototype.publishThresholdDeviation = function (level, message) {
    var id = new Date().getTime();
    id = i & 0xffffffff;
    this.publisher.publish({ ThresholdDeviation: { userUid: this.info.from, deviceName: this.deviceName, value: "" + this.value.toPrecision(3), noteLevel: "" + level, message: message, id: id } })
    this.lastTimeNotificationSent = (new Date()).getTime()
}




var sevice = module.exports = {
    analyze: function (info, response) {
        var threshold = THRESHOLD_MAP[info.name]
        if (!threshold) {
            logger.warn("@NotificationsService, no threshold for device: " + info.name)
            return
        }

        //setup for patient and leech onto publisher (this will be in ram but who cares at this time?)
        var publisher = publisherHandler.getPublisher(info.from)
        publisher[serviceName] || (publisher[serviceName] = {})
        publisher[serviceName][info.from] || (publisher[serviceName][info.from] = {})
        publisher[serviceName][info.from][info.name] || (publisher[serviceName][info.from][info.name] = threshold.newAnalyser(publisher, info))

        var analyser = publisher[serviceName][info.from][info.name]
        analyser.analyze(response.data)
        //console.log(this.result.state)
       /* publisher = publisherHandler.getPublisher(info.from)
        if (!publisher.realTimeCollectorMap) {
            publisher.realTimeCollectorMap = {}
            setTimeout(() => {
                publisher.publish({ [info.from]: publisher.realTimeCollectorMap })
                publisher.realTimeCollectorMap = null
            }, 750)
        }
        publisher.realTimeCollectorMap[info.name] = response.data.toPrecision(3)*/
    },
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
        patientManager.bindSubscriberListInfoHook({ Username: pubName }, (list) => {
            next(list)
        })
    }
})
