const webSockMessenger = require('../lib/webSockMessenger')
const patientManager = require("../patientManager");
var subscriberManager = require('../subscriberManager');

const logger = require('../revaLog')

const serviceName = 'UserManager'

webSockMessenger.attach(serviceName, {
    connect: function (transmitter) {
        //transmitter.transmit("--HELLO-- ");
    },
    close: function (transmitter) {
    },
    receiver: function (transmitter, obj) {
    },
    requirePersistentLink: true,
    defaultEnabled: true
    , channels: {
        REGISTER: {
            VALIDATE_EMAIL: function (transmitter, msg, key, channeler) {
                channeler({ PASS: true })
                //channeler({ PASS: false, ERROR: "This email address is not available" })
            },
            REGISTER_PATIENT: function (transmitter, msg, key, channeler) {
                var obj = msg;
                //TODO: Move to patiantManager
                function deserialize(obj) {
                    var test = require('../models/patientModel').fields
                    for (var key in test) {
                        switch (test[key].type) {
                            case 'int': { obj[key] && (obj[key] = parseInt(obj[key])) } break
                            case 'float': { obj[key] && (obj[key] = parseFloat(obj[key])) } break
                        }
                    }
                }
                deserialize(obj)
                PatientManager.getPatient(obj)
                    .then(function () {
                        channeler({ PATIENT_ERROR: 'This username has been taken', PATIENT_PASS: false })
                    }).catch(function () {
                        return PatientManager.addPatient(obj).then(function (pat) {
                            channeler({ PATIENT_PASS: true })
                        }).catch(function (e) {
                            channeler({ PATIENT_ERROR: e.message || e, PATIENT_PASS: false })
                        })
                    }).catch(function (e) {
                        logger.error('@webSockMessenger$UserManager#receiver:KEY_REGISTER_USER', e)
                        channeler({ PATIENT_ERROR: 'something went wrong', PATIENT_PASS: false })
                    })
            },
            REGISTER_NON_PATIENT: function (transmitter, msg, key, channeler) {
                var obj = msg;
                //TODO: Move to patiantManager
                function deserialize(obj) {
                    var test = require('../models/patientModel').fields
                    for (var key in test) {
                        switch (test[key].type) {
                            case 'int': { obj[key] && (obj[key] = parseInt(obj[key])) } break
                            case 'float': { obj[key] && (obj[key] = parseFloat(obj[key])) } break
                        }
                    }
                }
                deserialize(obj)
                SubscriberManager.getsubscriber(obj)
                    .then(function () {
                        //ugly prot i.e. copy and paste mostly form patient
                        channeler({ NON_PATIENT_ERROR: 'This email has been taken', NON_PATIENT_PASS: false })
                    }).catch(function () {
                        return SubscriberManager.addSubscriber(obj).then(function (pat) {
                            channeler({ NON_PATIENT_PASS: true })
                        }).catch(function (e) {
                            channeler({ NON_PATIENT_ERROR: e.message || e, NON_PATIENT_PASS: false })
                        })
                    }).catch(function (e) {
                        logger.error('@webSockMessenger$UserManager#receiver:KEY_REGISTER_USER', e)
                        channeler({ NON_PATIENT_ERROR: 'something went wrong', NON_PATIENT_PASS: false })
                    })
            }
        }
    }
})
