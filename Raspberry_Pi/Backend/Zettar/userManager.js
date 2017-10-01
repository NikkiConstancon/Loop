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
        const successMsg = "Your request has been sent"
        if (reqType === 'patient') {//this shoud not be manage externally, but what can you do???
            patientManager.getPatient({ Username: requester }).then(function (requester) {
                patientManager.getPatient({ Username: target }).then(function (target) {
                    requester.addPubSubRequestAsRequester(target.Username, passCb, failCb)
                    target.addPubSubRequestAsTarget(requester.Username)
                }).catch(function (e) {
                    subscriberManager.getsubscriber({ Email: target }).then(function (target) {
                        requester.addPubSubRequestAsRequester(target.Email, passCb, failCb)
                        target.addPubSubRequestAsTarget(requester.Username)
                    }).catch(function (e) {
                        failCb(e)
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
                    requester.addPubSubRequestAsRequester(target.Username, passCb, failCb)
                    target.addPubSubRequestAsTarget(requester.Email)
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
