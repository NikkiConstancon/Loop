//I am so frustrated with the db setup! Wish we just did what I initially suggested with a single account table with, that can be aggregated with different roles :(

const webSockMessenger = require('../lib/webSockMessenger')
const userManager = require('../userManager')
const patientManager = require("../patientManager");
var subscriberManager = require('../subscriberManager');

const logger = require('../revaLog')

const serviceName = 'PubSubBindingService'

webSockMessenger.attach(serviceName, {
    connect: function (transmitter) {
    },
    close: function (transmitter) {
    },
    receiver: function (transmitter, obj) {
    },
    onEnable: function (transmitter) {
        refreshInfo(transmitter)
    },
    defaultEnabled: true
    , channels: {
        BIND_PATIENT_AND_SUBSCRIBER: {
            REQ_BIND: function (transmitter, msg, key, channel) {
                userManager.pubSubBindRequest(
                    function (info) {
                        channel("")
                        webSockMessenger.transmitTo(serviceName, msg,
                            {
                                NEW_BINDING_CONFIRMATION_REQ: {
                                    type: info.type, state: info.state, userUid: transmitter.getUserUid()
                                }
                            })
                    },
                    function (errMsg) {
                        channel(errMsg.message || errMsg.clientSafe || "Oops! Something went wrong :(")
                    },
                    transmitter.getUserUid(),
                    msg,
                    transmitter.getUserType()
                )
            },
            ACCEPT: function (transmitter, msg, key, channel) {
                userManager.pubSubBindRequestOnDecision(transmitter.getUserUid(), msg, true, function (passed) {
                    channel(passed ? msg : "")
                })
            },
            DECLINE: function (transmitter, msg, key, channel) {
                userManager.pubSubBindRequestOnDecision(transmitter.getUserUid(), msg, false, function (passed) {
                    channel(passed ? msg : "")
                })
            },
            DROP_PUB_SUB_BINDING_AS_SUB: function (transmitter, msg, key, channel) {
                userManager.dropPubSubBinding(transmitter.getUserUid(), msg, function (removed) {
                    if (removed) {
                        channel(msg)
                    } else {
                        channel("")
                    }
                })
            },
            DROP_PUB_SUB_BINDING_AS_PAT: function (transmitter, msg, key, channel) {
                userManager.dropPubSubBinding(msg, transmitter.getUserUid(), function (removed) {
                    if (removed) {
                        channel(msg)
                    } else {
                        channel("")
                    }
                })
            }
        }
    }
})



function refreshInfo(transmitter) {
    setTimeout(function () {
        try {
            if (transmitter.getUserType && transmitter.getUserType() === 'patient') {
                patientManager.getPatient({ Username: transmitter.getUserUid() }).then(function (pat) {
                    try {
                        var tmp = {}
                        for (var user in pat.PubSubBindingConfirmationMap) {
                            tmp[user] = JSON.parse(pat.PubSubBindingConfirmationMap[user])
                        }
                        transmitter.transmit({ BINDING_CONFIRMATION_REQ: tmp })
                        transmitter.transmit({ PATIENT_LIST: pat.PatientList || [] })
                        transmitter.transmit({ SUBSCRIBER_LIST: pat.SubscriberList || [] })
                        transmitter.transmit({ DONE: true })
                    } catch (e) {
                        logger.error(e)
                    }
                });
            } else {
                subscriberManager.getsubscriber({ Email: transmitter.getUserUid() }).then(function (sub) {
                    try {
                        var tmp = {}
                        for (var user in sub.PubSubBindingConfirmationMap) {
                            tmp[user] = JSON.parse(sub.PubSubBindingConfirmationMap[user])
                        }
                        transmitter.transmit({ BINDING_CONFIRMATION_REQ: tmp })
                        transmitter.transmit({ PATIENT_LIST: sub.PatientList || [] })
                        //transmitter.transmit({ SUBSCRIBER_LIST: sub.SubscriberList || [] })
                        transmitter.transmit({ DONE: true })
                    } catch (e) {
                        logger.error(e)
                    }
                });
            }
        } catch (e) {
            logger.error("I AM SO OVER THIS", e)
        }
    }, 333);
}



module.exports = {
    update: function (userUid, PubSubBindingConfirmationMap, PatientList, SubscriberList) {
        transmitters = webSockMessenger.getTransmitterArr(serviceName, userUid)
        for (var i in transmitters) {
            refreshInfo(transmitters[i])
        }
    }
}

