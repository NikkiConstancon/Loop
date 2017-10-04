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
                refreshInfo(transmitter)
                channel(msg)//for now
                otherTransmitters = webSockMessenger.getTransmitters(msg)
                for (var devId in getTransmitters(msg)) {
                    refreshInfo(otherTransmitters[devId])
                }
            },
            DECLINE: function (transmitter, msg, key, channel) {
                userManager.pubSubBindRequestOnDecision(transmitter.getUserUid(), msg, false, function (passed) {
                    channel(passed ? msg : "")
                })
                refreshInfo(transmitter)
                otherTransmitters = webSockMessenger.getTransmitters(msg)
                for (var devId in getTransmitters(msg)) {
                    refreshInfo(otherTransmitters[devId])
                }
            },
            DROP_PATIENT_AND_SUBSCRIBER: function () {

            }
        }
    }
})



function refreshInfo(transmitter) {
    if (transmitter.getUserType() === 'patient') {
        patientManager.getPatient({ Username: transmitter.getUserUid() }).then(function (pat) {
            for (var user in pat.PubSubBindingConfirmationMap) {
                transmitter.transmit({ BINDING_CONFIRMATION_REQ: { [user]: JSON.parse(pat.PubSubBindingConfirmationMap[user]) } })
            }
            transmitter.transmit({ PATIENT_LIST: pat.PatientList || [] })
            transmitter.transmit({ SUBSCRIBER_LIST: pat.SubscriberList || [] })
            transmitter.transmit({ DONE:true })
        });
    } else {
        subscriberManager.getsubscriber({ Email: transmitter.getUserUid() }).then(function (sub) {
            for (var user in sub.PubSubBindingConfirmationMap) {
                transmitter.transmit({ BINDING_CONFIRMATION_REQ: { [user]: JSON.parse(sub.PubSubBindingConfirmationMap[user]) } })
            }
            transmitter.transmit({ PATIENT_LIST: sub.PatientList || [] })
            transmitter.transmit({ SUBSCRIBER_LIST: pat.SubscriberList || [] })
            transmitter.transmit({ DONE: true })
        });
    }
}


