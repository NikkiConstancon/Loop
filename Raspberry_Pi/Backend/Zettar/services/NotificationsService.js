const webSockMessenger = require('../lib/webSockMessenger')
const patientManager = require("../patientManager");

const subscriberManager = require('../subscriberManager');
const dataManager = require('../patientDataManager');
const logger = require('../revaLog')

const serviceName = 'Notifications'



var sevice = module.exports = {
    notify: function (info, response) {
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
        next([pubName])
    }
})
