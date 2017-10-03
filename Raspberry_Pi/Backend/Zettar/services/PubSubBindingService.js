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
                        webSockMessenger.transmitTo(serviceName, msg, { NEW_BINDING_CONFIRMATION_REQ: { type: info.type, state: info.state, userUid: msg } })
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
                userManager.pubSubBindRequestOnDecision(transmitter.getUserUid(), msg, true)
                refreshInfo(transmitter)
                channel(true)//for now
            },
            DECLINE: function (transmitter, msg, key, channel) {
                userManager.pubSubBindRequestOnDecision(transmitter.getUserUid(), msg, false)
                refreshInfo(transmitter)
                channel(true)//for now
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
        });
    } else {
        subscriberManager.getsubscriber({ Email: transmitter.getUserUid() }).then(function (sub) {
            for (var user in sub.PubSubBindingConfirmationMap) {
                transmitter.transmit({ BINDING_CONFIRMATION_REQ: { [user]: JSON.parse(sub.PubSubBindingConfirmationMap[user]) } })
            }
            transmitter.transmit({ PATIENT_LIST: sub.PatientList || [] })
        });
    }
}


