const webSockMessenger = require('../lib/webSockMessenger')
const patientManager = require("../patientManager");

const subscriberManager = require('../subscriberManager');
const dataManager = require('../patientDataManager');
const logger = require('../revaLog')

const serviceName = 'Notifications'
const defaultSmoothFactor = 0.3 // (f * now) + (1-f)*last
//"°C"
const THRESHOLD_MAP = {
    "Body Temperature": new Threshold(0, 1000, defaultSmoothFactor, true),
    "Body Insulin": new Threshold(0, 1000, defaultSmoothFactor, true),
    "Heart Rate": new Threshold(0, 1000, defaultSmoothFactor, true),
    "Body Glucose": new Threshold(37.2, 38.9, defaultSmoothFactor, true),
    
    
    "Blood Pressure": new Threshold(60, 100, defaultSmoothFactor, false),
    "Diastolic Pressure": new Threshold(60, 79, defaultSmoothFactor, false),
    "Systolic Pressure": new Threshold(90, 119, defaultSmoothFactor, false),
    
    "Oxygen": new Threshold(75, 100, defaultSmoothFactor, true),
    "Pulse": new Threshold(60, 100, defaultSmoothFactor, true),
    
    "ECG": new Threshold(0, 6, defaultSmoothFactor, true),
    
    "Body Fat": new Threshold(18, 31, defaultSmoothFactor, false),
    "Bady water": new Threshold(45, 65, defaultSmoothFactor, false),
    "Calories": new Threshold(900, 1000, defaultSmoothFactor, false),
    "Bone Density": new Threshold(45, 65, defaultSmoothFactor, false),
    "Muscle Mass": new Threshold(13, 21, defaultSmoothFactor, false),
    "Visceral Fat": new Threshold(0, 13, defaultSmoothFactor, false),
    "Weight": new Threshold(45, 200, defaultSmoothFactor, false),
    
    "Airflow": new Threshold(376, 525, defaultSmoothFactor, false),
    "Air Volume": new Threshold(2, 5, defaultSmoothFactor, false),



    "dummy dev": new Threshold(2.5, 2.5, defaultSmoothFactor, false),
}

setInterval(function () {
    try {
        sevice.analyze({ from: "greg", name: "dummy dev" }, { data: 2 + Math.random() })
    } catch (e) { logger.error(e) }
}, 2 * 60 * 1000)


setInterval(function () {
    try {
        sevice.analyze({ from: "nikki", name: "dummy dev" }, { data: 2 + Math.random() })
        sevice.analyze({ from: "juan", name: "dummy dev" }, { data: 2 + Math.random() })
        sevice.analyze({ from: "rinus", name: "dummy dev" }, { data: 2 + Math.random() })
    } catch (e) { logger.error(e) }
}, 4.13 * 60 * 1000)

function Threshold(min, max, smoothFactor, flagRealtimeAnalysis, unit){
    this.min = min
    this.max = max
    this.smoothFactor = smoothFactor
    this.flagRealtimeAnalysis = flagRealtimeAnalysis
    this.unit = unit
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
    this.thrashGuardDelay = 3 * 60 * 1000
}

function RandLevel() {
    return (Math.random() * 3).toPrecision(1)
}

Analyser.prototype.analyze = function (now) {
    if (((new Date()).getTime() - this.lastTimeNotificationSent) > this.thrashGuardDelay) {
         if (this.value == null) {
                this.value = now
                this.smooth = now
        }
        if (this.threshold.flagRealtimeAnalysis) {
           
            this.updateSmooth(now)
            
            if (this.smooth < this.threshold.min) {
                this.publishThresholdDeviation(RandLevel(), this.deviceName + " is below the set threshold at " + this.valueWithUnit(now))
            } else if (this.smooth > this.threshold.max) {
                this.publishThresholdDeviation(RandLevel(), this.deviceName + " is above the set threshold at " + this.valueWithUnit(now))
            }
        } else {
            if (this.value < this.threshold.min) {
                this.publishThresholdDeviation(RandLevel(), this.deviceName + " is below the set threshold at " + this.valueWithUnit(now))
            } else if (this.value > this.threshold.max) {
                this.publishThresholdDeviation(RandLevel(), this.deviceName + " is above the set threshold at " + this.valueWithUnit(now))
            }
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
    id = id & 0xffffffff;
    this.publisher.publish({ ThresholdDeviation: { userUid: this.info.from, deviceName: this.deviceName, value: "" + this.value.toPrecision(3), noteLevel: "" + level, message: message, id: id + "" } })
    this.lastTimeNotificationSent = (new Date()).getTime()
}


const memoryLeakMap = {}//just want to fix the bug (no time left)

function Publisher(userUid) {
    this.userUid = userUid
}
//this is not how WebSocketServes should have work :|
//this is a quick fix 
Publisher.prototype.publish = function (msg) {
    patientManager.getPatient({ Username: this.userUid }).then(pat => {
        var arr = pat.SubscriberList || []
        arr.push(this.userUid)
        for (var ii in arr) {
            user = arr[ii]
            transmitters = webSockMessenger.getTransmitterArr(serviceName, user)
            for (var i in transmitters) {
                transmitters[i].transmit(msg)
            }
        }
    }).catch(function (e) { logger.error(e) })
}
var sevice = module.exports = {
    analyze: function (info, response) {
        var threshold = THRESHOLD_MAP[info.name]
        if (!threshold) {
            logger.warn("@NotificationsService, no threshold for device: " + info.name)
            return
        }

        //setup for patient and leech onto publisher (this will be in ram but who cares at this time?)
        memoryLeakMap[serviceName] || (memoryLeakMap[serviceName] = {})
        memoryLeakMap[serviceName][info.from] || (memoryLeakMap[serviceName][info.from] = {})
        memoryLeakMap[serviceName][info.from][info.name] || (memoryLeakMap[serviceName][info.from][info.name] = threshold.newAnalyser(new Publisher(info.from), info))

        var analyser = memoryLeakMap[serviceName][info.from][info.name]
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
webSockMessenger.attach(serviceName, {
    defaultEnabled: true,
    connect: function (transmiter) {
        //publisher = publisherHandler.getPublisher(transmiter.getUserUid())
        //publisher.setMeta({nice:"adsfasdf"})
    },
    close: function (context) {
    },
    receiver: function (transmiter, obj) {
        transmiter.transmit(obj)
    }
})
