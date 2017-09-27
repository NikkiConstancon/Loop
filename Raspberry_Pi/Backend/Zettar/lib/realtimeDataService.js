﻿const webSockMessenger = require('./webSockMessenger')
const patientManager = require("../patientManager");
var subscriberManager = require('../subscriberManager');

const logger = require('../revaLog')

const serviceName = 'RTDS'


var sevice = module.exports = {
    publish: function (info, response) {
        publisher = publisherHandler.getPublisher(info.from)
        if (!publisher.realTimeCollectorMap) {
            publisher.realTimeCollectorMap = {}
            setTimeout(() => {
                publisher.publish({ [info.from]: publisher.realTimeCollectorMap })
                publisher.realTimeCollectorMap = null
            }, 750)
        }
        publisher.realTimeCollectorMap[info.name] = response.data.toPrecision(3)
    },
    connectZettalet: function (zettaletName) {
    },
    disconnectZettalet: function (zettaletName) {
        publisher = publisherHandler.getPublisher(zettaletName)
        publisher.setMeta({})
    },
    connectDevice: function (zettaletName, info) {
        publisher = publisherHandler.getPublisher(zettaletName)
        publisher.setMetaField(info.name, { type: info.type, topicName: info.topicName })
    }
}

var publisherHandler = webSockMessenger.attach(serviceName, {
    connect: function (transmiter) {
    },
    close: function (context) {
    },
    receiver: function (transmiter, obj) {
        transmiter.transmit(obj)
    },
    subListUpdater: function (pubName, next) {
        patientManager.bindSubscriberListInofHook({ Username: pubName }, (list) => {
            next(list)
        })
    }
})
