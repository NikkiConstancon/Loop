'use strict';

//TODO: in future we may want to add a device id list to each patient. That way we can keep track f what devices each patient has



var dbMan = require('./databaseManager');
var util = require('util');
var logger = require('./revaLog');

//ENCRYPTION:
var CryptoJS = require("crypto-js");

var patientKey = "xP{}Lk.x#3V2S?F2p'q{kqd[Qu{7/S-d*bzt"
var accessKey = "4]),`~>{CKjv(E@'d:udH6N@/G4n(}4dn]Mi"

var keys = require('./lib/keys')

/*EXAMPLE USE
// Encrypt 
var ciphertext = CryptoJS.AES.encrypt('my message', 'secret key 123');

// Decrypt 
var bytes  = CryptoJS.AES.decrypt(ciphertext.toString(), 'secret key 123');
var plaintext = bytes.toString(CryptoJS.enc.Utf8);
console.log(plaintext);
*/

/*TABLE STRUCTURE:
module.exports = {
    fields:{
        Username    : "text",           
        Password    :{  ----> stored as key-value pairs --> as generated by crypto-js
            type: "map",
            typeDef: "<varchar, text>"
        },
        AccessPassword: {    ----> stored as key-value pairs --> as generated by crypto-js
            type: "map",
            typeDef: "<varchar, text>"
        },
        SubscriberList : {      ---> just an ordered array of emails
            type: "set",
            typeDef: "<text>"
        }, 
        Email     : "text",  
        Address     : "text", 
        Age     : "int",    
        Weight     : "int",
        Height     : "int",
        Reason     : "text" 
    },
    key:["Username"]        
}
*/

var patientManager = module.exports = {
    addPatient: function (_patient){
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                 dbMan.models.instance.patient.findOne({ Username: _patient.Username }, function (err, found) {
                    if (err) {
                        reject(err)
                    }
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
                    }
                });
            }).catch((err)=>{
                reject(err)
            })
        })
    },
    getPatient: function (_patient) {
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                dbMan.models.instance.patient.findOne({ Username: _patient.Username }, function (err, found) {
                    if (err || !found) {
                        err = err || 'invalid username'
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
    addToSubscriberList: function(_patient,_newSubscriber){
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
                    if (err) {
                        logger.error(err)
                        reject(err)
                    }
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
                    var query_object = {Username: _patient.Username};
                    var update_values_object = {SubscriberList: updateValue};
                    dbMan.models.instance.patient.update(query_object, update_values_object, null, function(err){
                        if(err) console.log(err);
                        else console.log('Yuppiie!');
                    });
                    resolve(found)
                })

            }).catch((err) => {
                reject(err)
            })
        })
    },
    removeFromSubscriberList: function(_patient,_oldSubscriber){
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                  //Find 
                dbMan.models.instance.patient.findOne({ Username: _patient.Username }, function (err, found) {
                    if (err) {
                        logger.error(err)
                        reject(err)
                    }
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
                    var query_object = {Username: _patient.Username};
                    var update_values_object = {SubscriberList: updateValue};
                    dbMan.models.instance.patient.update(query_object, update_values_object, null, function(err){
                        if(err) console.log(err);
                        else console.log('Yuppiie!');
                    });
                    resolve(found)
                })
            }).catch((err) => {
                reject(err)
            })
        })
    },
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
                    } else if (found.RegistrationObject.k1 === key1 && found.RegistrationObject.k2 === key2) {
                        dbMan.models.instance.patient.update(who, { RegistrationObject: { c: 'registered' } }, function (err) {
                            if (err) { console.log(err); reject(err) } else { resolve(found)}
                        })
                    } else {
                        reject('mismatched keys')
                    }
                })
            }).catch((err) => {
                reject(err)
            })
        })
    },
    getModelInfo: function () {
        return 'keyspace ' + dbMan.getKeyspcaeName() + ', tabel patient';
    }
}