'use strict';


/**
 * @file
 * This file implements a singleton design pattern to allow easy access to users of the system.
 * It also simplifies database queries and provides error correction in events that such capabilities
 * are needed.
 *
 * @TODO: in future we may want to add a device id list to each patient. That way we can keep track f what devices each patient has
 **/


var dbMan = require('./databaseManager');
var util = require('util');
var logger = require('./revaLog');

//ENCRYPTION:
var CryptoJS = require("crypto-js");

var patientKey = "xP{}Lk.x#3V2S?F2p'q{kqd[Qu{7/S-d*bzt"
var accessKey = "4]),`~>{CKjv(E@'d:udH6N@/G4n(}4dn]Mi"

var keys = require('./lib/keys')
var sharedKeys = require('../Shared/sharedKeys')


var subscriberManager = require("./subscriberManager")

/**EXAMPLE USE
 * Encrypt 
 *var ciphertext = CryptoJS.AES.encrypt('my message', 'secret key 123');
 *
 * Decrypt 
 *var bytes  = CryptoJS.AES.decrypt(ciphertext.toString(), 'secret key 123');
 *var plaintext = bytes.toString(CryptoJS.enc.Utf8);
 *console.log(plaintext);
 *
 *
 *TABLE STRUCTURE:
 *module.exports = {
 *    fields:{
 *        Username    : "text",           
 *        Password    :{  ----> stored as key-value pairs --> as generated by crypto-js
 *            type: "map",
 *            typeDef: "<varchar, text>"
 *        },
 *        AccessPassword: {    ----> stored as key-value pairs --> as generated by crypto-js
 *            type: "map",
 *            typeDef: "<varchar, text>"
 *        },
 *        SubscriberList : {      ---> just an ordered array of emails
 *            type: "set",
 *            typeDef: "<text>"
 *        }, 
 *        Email     : "text",  
 *        Address     : "text", 
 *        Age     : "int",    
 *        Weight     : "int",
 *        Height     : "int",
 *        Reason     : "text" 
 *    },
 *    key:["Username"]        
 *}
 **/

var patientManager = module.exports = {
    /**
     *@brief adds a new user to the database
     *
     *@return a promise passing the newly added user to the function called back
     */
    addPatient: function (_patient){
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                 dbMan.models.instance.patient.findOne({ Username: _patient.Username }, function (err, found) {
                    if (err) {
                        err = err || { clientSafe: 'Could not find ' + _patient.Username }
                        logger.error(err)
                        reject(err)
                    }{
                        if(!found){
                            var newPatient = new dbMan.models.instance.patient(_patient)
                            newPatient.save(function (err) {
                                if (err) {
                                    logger.error(err)
                                    reject(err)
                                } else {
                                    logger.debug('Patient Added Successfully!')
                                    resolve(newPatient)
                                }
                            })
                            //resolve(found)

                        }else{
                            //WARNING USER NAME IS TAKEN code goes here
                            logger.debug('ERROR: ' + found.Username + " already exists")
                            reject( found.Username + ' already exists')
                            //resolve(null);
                        }
                    }
                });
            }).catch((err)=>{
                reject(err)
            })
        })
    },
    getDeviceMap: function(_patient){
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                var q = {}
                if (_patient.Email) {
                    q.Email = _patient.Email
                } else {
                    q.Username = _patient.Username
                }
                dbMan.models.instance.patient.findOne({ Username: _patient.Username }, function (err, found) {
                    if (err || !found) {
                        err = err || { clientSafe: 'Could not find ' + _patient.Username }
                        logger.error(err)
                        reject(err)
                    } else {
                        logger.debug('Found patient: ' + found.Username)
//                        console.log(found.DeviceMap);
                        resolve(found.DeviceMap)
                    }
                })
            }).catch((err) => {
                reject(err)
            })
        })
        
    },
    addToDeviceMap: function(_patient, deviceName, On){
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                //Find 
                console.log(_patient.Username)
                dbMan.models.instance.patient.findOne({ Username: _patient.Username }, function (err, found) {
                   if (err || !found) {
                        err = err || { clientSafe: 'Could not find ' + _patient.Username }
                        logger.error(err)
                        reject(err)
                    } else {
                        //if user does not exist
                        if(!found){
                            resolve("NotFound");
                        }
                        if(found == "undefined"){
                            resolve("NotFound");
                        }
    console.log("found");
                        var updateValue ;
                        if(found.DeviceMap == null){
                            //if list does not exist
                            console.log("inner")
                            updateValue = {};
                            updateValue[deviceName] =  On;
                        }else{
                            updateValue = found.DeviceMap;
                            //toggle value to on or off
                            updateValue[deviceName] = On;
                        }
                        
                        console.log(updateValue);
                        // var query_object = {Username: _patient.Username};
                        // var update_values_object = {DeviceMap: updateValue};
                        found.DeviceMap = updateValue;
                        found.save(function (err) {
                            resolve(found)
                        })
                        // dbMan.models.instance.patient.update(query_object, update_values_object, null, function(err){
                        //     if(err) console.log(err);
                        //     else console.log('Yuppiie!');
                        // });
                    }
                })
            })
        })

    },
    /**
     *@brief gets a user from the database
     *
     *@return a promise passing the newly added user to the function called back
     */
    getPatient: function (_patient) {
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                var q = {}
                if (_patient.Email) {
                    q.Email = _patient.Email
                } else {
                    q.Username = _patient.Username
                }
                dbMan.models.instance.patient.findOne({ Username: _patient.Username }, function (err, found) {
                    if (err || !found) {
                        err = err || { clientSafe: 'Could not find ' + _patient.Username }
                        logger.error(err)
                        reject(err)
                    } else {
                        logger.debug('Found patient: ' + found.Username)
                        resolve(found)
                    }
                })
            }).catch((err) => {
                reject(err)
            })
        })
    },

    /**
     *This will replace getPatient.
     *@brief checks if the username specified already exits.
     *
     *@return a promise passing a boolean value to the function called back
     */
    checkUsernameExists: function (_username) {
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                dbMan.models.instance.patient.findOne({ Username: _username }, function (err, found) {
                    if (err || !found) {
                        err = err || 'no username exits'
                        logger.debug(err)
                        resolve(false)
                    } else {
                        logger.debug('Found username: ' + found.Username)
                        resolve(true)
                    }
                })
            }).catch((err) => {
                reject(err)
            })
        })
    },


    /**
     *@brief adds a Subscriber to the Patients list of users subscribed to them.
     *
     *@return a promise passing the newly added user to the function called back
     */
  /*  addToSubscriberList: function(_patient,_newSubscriber){
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                //Validate email:
                console.log("validating")
                var re = /\S+@\S+\.\S+/;
                if(!re.test(_newSubscriber)){
                    resolve(null); //need to test if this is right
                }
                console.log("valid " )

                //Find 
                dbMan.models.instance.patient.findOne({ Username: _patient.Username }, function (err, found) {
                    if (err || ! found) {
                        err = err || { clientSafe: 'Could not find ' + _patient.Username }
                        logger.error(err)
                        reject(err)
                    } else {
                        //now update
                        logger.debug('Found patient: ' + found.SubscriberList)
                        
                        //is that subscriber already on the list?
                        var updateValue;
                        if(found.SubscriberList == null){
                            updateValue = [_newSubscriber]
                        }else{
                            updateValue = found.SubscriberList
                            if(updateValue.indexOf(_newSubscriber) > -1){
                                //already on list
                                reject(null);
                                return null;
                            }
                            updateValue.push(_newSubscriber)
                        }


                        //store updated value
                        console.log(updateValue)
                        found.SubscriberList = updateValue;
                        found.save(function(err){});
                        // var query_object = {Username: _patient.Username};
                        // var update_values_object = {SubscriberList: updateValue};
                        // dbMan.models.instance.patient.update(query_object, update_values_object, null, function(err){
                        //     if(err) console.log(err);
                        //     else console.log('Yuppiie!');
                        // });
                        resolve(found)
                    }
                })

            }).catch((err) => {
                reject(err)
            })
        })
    },*/


    /**
     *@brief gets a Subscriber from the database
     *
     *@return a promise passing the newly added user to the function called back
     */
    /*removeFromSubscriberList: function(_patient,_oldSubscriber){
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                  //Find 
                dbMan.models.instance.patient.findOne({ Username: _patient.Username }, function (err, found) {
                    if (err || ! found) {
                        err = err || { clientSafe: 'Could not find ' + _patient.Username }
                        logger.error(err)
                        reject(err)
                    } else{
                        //now remove
                        logger.debug('Found patient: ' + found.SubscriberList)
                        
                        //is that subscriber on the list?
                        var updateValue;
                        if(found.SubscriberList == null){
                            resolve("notOnList");
                            return null;
                        }else{
                            updateValue = found.SubscriberList
                            if(updateValue.indexOf(_oldSubscriber) < -1){
                                //already on list
                                resolve("notOnList");
                                return null;
                            }
                            updateValue.splice(_oldSubscriber.indexOf(_oldSubscriber),1)
                        }
                        //store updated value
                        console.log(updateValue)
                        found.SubscriberList = updateValue;
                        found.save(function(err){});
                        // var query_object = {Username: _patient.Username};
                        // var update_values_object = {SubscriberList: updateValue};
                        // dbMan.models.instance.patient.update(query_object, update_values_object, null, function(err){
                        //     if(err) console.log(err);
                        //     else console.log('Yuppiie!');
                        // });
                        resolve(found)
                    }
                })
            }).catch((err) => {
                reject(err)
            })
        })
    },*/
    /**
     * @brief bind the patients Zettalet's URI to the specific user
     **/
    bindZettalet: function (key, apiUri) {
        return new Promise(function (res, rej) {
            var who = { Username:  key}//sharedKeys.decrypt(key)
            patientManager.getPatient(who)
                .then(function (pat) {
                    dbMan.models.instance.patient.update(who, { ZettaletUuid: apiUri}, function (err) {
                        if (err) {
                            rej(err)
                        } else {
                            res(pat)
                        }
                    })
                }).catch(function (e) {
                    rej(e)
                })
        })
    },
    /**
     *@brief handel the logic to activate the user's email based on a received URI
     **/
    validateEmail: function (key1, key2) {
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                //Find 
                
                var who = { Username: keys.userEmailDecrypt(key1) }
                if (!who.Username) {
                    reject('expired or invalid key1')
                    return
                }
                dbMan.models.instance.patient.findOne(who, function (err, found) {
                    if (err || !found) {
                        logger.error('email registration failed could not find: ' + who.Username)
                        reject(err)
                    } else {
                        if (found.RegistrationObject.k2 === key2) {
                            dbMan.models.instance.patient.update(who, { RegistrationObject: { c: 'registered' } }, function (err) {
                                if (err) { console.log(err); reject(err) } else { resolve('sucsess') }
                            })
                        } else if (found.RegistrationObject.k3 === key2){
                            dbMan.models.instance.patient.update(who, { RegistrationObject: { c: 'declined' } }, function (err) {
                                if (err) { console.log(err); reject(err) } else { resolve('declined') }
                            })
                        } else {
                            reject('mismatched keys')
                        }
                    }
                })
            }).catch((err) => {
                reject(err)
            })
        })
    },
    triggerUpdateForSubscriberListInfoHook: function (pat, newSub) {
        if (SubscriberListInofHookMap[pat.Username]) {
            var list = pat.getSubscriberList()
            if (list.indexOf(newSub) == -1) {
                list.push(newSub)
            }
            SubscriberListInofHookMap[pat.Username](list)
        }
    },
    bindSubscriberListInfoHook: function (userInfo, cb) {
        var userUid = userInfo.Username || userInfo.Email
        if (SubscriberListInofHookMap[userUid]) {
            try {
                throw "@PatientManager.bindSubscriberListInfoHook: already hooked for " + JSON.stringify(userInfo)
            } catch (e) {
                logger.warn(e)
            }
        }
        SubscriberListInofHookMap[userUid] = cb

        patientManager.getPatient(userInfo).then(function (pat) {
            SubscriberListInofHookMap[pat.Username](pat.getSubscriberList())
        }).catch(() => { })
    },
    unbindSubscriberListInfoHook: function (userInfo, cb) {
        var userUid = userInfo.Username || userInfo.Email
        if (SubscriberListInofHookMap[userUid]) {
            delete SubscriberListInofHookMap[userUid]
        }
    },
    getModelInfo: function () {
        return 'keyspace ' + dbMan.getKeyspcaeName() + ', tabel patient';
    }
}


var SubscriberListInofHookMap = {}





