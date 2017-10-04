'use strict';
/**
 * @file
 * This file implements a singleton design pattern to allow easy access to users of the system.
 * It also simplifies database queries and provides error correction in events that such capabilities
 * are needed.
 **/


var dbMan = require('./databaseManager');
var util = require('util');
var logger = require('./revaLog');

const patientManager = require("./patientManager");
var subscriberManager = require('./subscriberManager');
const userManagerUtil = require('./userManagerUtil')


var UserManager = module.exports = {
    //All so wrong all the way
    doOnUser: function (userUid, then, error, hint) {
        if (hint) {
            if (transmitter.getUserType() === 'patient') {
                patientManager.getPatient({ Username: transmitter.getUserUid() }).then(function (pat) {
                    then && then(pat)
                }).catch(function (e) {
                    error && error(e)
                })
            } else {
                subscriberManager.getsubscriber({ Email: transmitter.getUserUid() }).then(function (sub) {
                    then && then(sub)
                }).catch(function (e) {
                    error && error(e)
                })
            }
        } else {
            patientManager.getPatient({ Username: transmitter.getUserUid() }).then(function (pat) {
                then && then(pat)
            }).catch(function (e) {
                subscriberManager.getsubscriber({ Email: transmitter.getUserUid() }).then(function (sub) {
                    then && then(sub)
                }).catch(function (e) {
                    error && error(e)
                })
            })
        }
    },
    pubSubBindRequest: function (passCb, failCb, requester, target, reqType) {
        if (target == requester) {
            failCb({ clientSafe: "You cannot add yourself" })
            return
        }
        const successMsg = "Your request has been sent"
        if (reqType === 'patient') {//this shoud not be manage externally, but what can you do???
            patientManager.getPatient({ Username: requester }).then(function (requester) {
                patientManager.getPatient({ Username: target }).then(function (target) {
                    requester.addPubSubRequestAsRequester(target.Username, false)
                    target.addPubSubRequestAsTarget(requester.Username, true, passCb, failCb)
                }).catch(function (e) {
                    subscriberManager.getsubscriber({ Email: target }).then(function (target) {
                        requester.addPubSubRequestAsRequester(target.Email, true)
                        target.addPubSubRequestAsTarget(requester.Username, false, passCb, failCb)
                    }).catch(function (e) {
                        failCb({ clientSafe: "No such account" })
                    })
                })
            }).catch(function (e) {
                try {
                    throw e
                } catch (e) {
                    logger.error("@UserManager#pubSubBindRequest:", e)
                }
                failCb(e)
            })
        } else {
            subscriberManager.getsubscriber({ Email: requester }).then(function (requester) {
                patientManager.getPatient({ Username: target }).then(function (target) {
                    requester.addPubSubRequestAsRequester(target.Username, false)
                    target.addPubSubRequestAsTarget(requester.Email, true, passCb, failCb)
                }).catch(function () {
                    failCb({ clientSafe: "You cannot subscribe to this user" })
                })
            }).catch(function (e) {
                try {
                    throw e
                } catch (e) {
                    logger.error("@UserManager#pubSubBindRequest:", e)
                }
                failCb(e)
            })
        }
    },
    pubSubBindRequestOnDecision: function (acceptor, requester, decision, then) {
        const tmpErrorMsg = "GREG! the person sending the request should not be able to accept"
        patientManager.getPatient({ Username: acceptor }).then(function (acc) {
            patientManager.getPatient({ Username: requester }).then(function (req) {
                if (decision && JSON.parse(acc.PubSubBindingConfirmationMap[requester]).type == userManagerUtil.enum.pubSubReq.type.request) {
                    logger.error(tmpErrorMsg)
                    then(false)
                }
                req.pubSubRequestOnDecision(acceptor, decision)
                acc.pubSubRequestOnDecision(requester, decision)
                then(true)
            }).catch(function () {
                subscriberManager.getsubscriber({ Email: requester }).then(function (req) {
                    if (decision && JSON.parse(acc.PubSubBindingConfirmationMap[requester]).type == userManagerUtil.enum.pubSubReq.type.request) {
                        logger.error(tmpErrorMsg)
                        then(false)
                    }
                    req.pubSubRequestOnDecision(acceptor, decision)
                    acc.pubSubRequestOnDecision(requester, decision)
                    then(true)
                })
            })
        }).catch(function () {
            subscriberManager.getsubscriber({ Email: acceptor }).then(function (acc) {
                patientManager.getPatient({ Username: requester }).then(function (req) {
                    if (decision && JSON.parse(acc.PubSubBindingConfirmationMap[requester]).type == userManagerUtil.enum.pubSubReq.type.request) {
                        logger.error(tmpErrorMsg)
                        then(false)
                    }
                    req.pubSubRequestOnDecision(acceptor, decision)
                    acc.pubSubRequestOnDecision(requester, decision)
                    then(true)
                })
            })
        })
    }
}
