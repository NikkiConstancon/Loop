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


var UserManager = module.exports = {
    //All so wrong all the way
    pubSubBindRequest: function (passCb, failCb, requester, target, reqType) {
        if (target == requester) {
            failCb({ clientSafe: "You cannot add yourself" })
            return
        }
        const successMsg = "Your request has been sent"
        if (reqType === 'patient') {//this shoud not be manage externally, but what can you do???
            patientManager.getPatient({ Username: requester }).then(function (requester) {
                patientManager.getPatient({ Username: target }).then(function (target) {
                    requester.addPubSubRequestAsRequester(target.Username)
                    target.addPubSubRequestAsTarget(requester.Username, passCb, failCb)
                }).catch(function (e) {
                    subscriberManager.getsubscriber({ Email: target }).then(function (target) {
                        requester.addPubSubRequestAsRequester(target.Email)
                        target.addPubSubRequestAsTarget(requester.Username, passCb, failCb)
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
                    requester.addPubSubRequestAsRequester(target.Username)
                    target.addPubSubRequestAsTarget(requester.Email, passCb, failCb)
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
    }
}
