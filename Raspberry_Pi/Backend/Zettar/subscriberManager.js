'use strict';

//TODO: in future we may want to add a device id list to each subscriber. That way we can keep track f what devices each subscriber has



var dbMan = require('./databaseManager');
var util = require('util');
var logger = require('./revaLog');

//ENCRYPTION:
var CryptoJS = require("crypto-js");

const ENCRYPT_KEY = "xP{}Lk.x#3V2S?F2p'q{kqd[Qu{7/S-d*bzt";

var keys = require('./lib/keys');


function encrypt(value) {
    return CryptoJS.AES.encrypt(value, 'secret key 123').toString()
}
function decrypt(value) {
    return CryptoJS.AES.decrypt(value, 'secret key 123').toString(CryptoJS.enc.Utf8)
}

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
    fields: {
    Email: {
            type: "text",
            rule: {
                validator: function (email) {
                    var re = /\S+@\S+\.\S+/;
                    return re.test(email);
                },
                message: 'Email is in the incorrect form',
                required: true
            }
        },
    Password    : {
            type: "text",
            default: function () { return encrypt(uuidv1()) },
            rule: {
                required: true
            }
        },
    Relation : {
            type: "text",
            rule: {
                validator: function (value) {
                    var val = value.toLowerCase();
                    if (val == 'family' || val == 'caretaker' || val == 'researcher' || val == 'doctor')
                        return true;
                    return false;
                },
                message: 'Relation is not included in the set of reasons.'
            }
        },
    PatientList     : {
            type: "list",
            typeDef: "<text>"
        },
    }
    key:["Email"]        
}
*/
 var validatePatient = function(_userName_Password){
                        
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                dbMan.models.instance.patient.findOne({ Username: _userName_Password.Username }, function (err, found) {
                    if (err || !found) {
                        err = err || 'could not find ' + _userName_Password.Username
                        logger.error(err)
                        reject(err)
                    } else {
                        logger.debug('Found patient: ' + found.Username)
                           // console.log(_userName_Password.AccessPassword);

                        if (_userName_Password.AccessPassword == decrypt(found.AccessPassword)) {
                            console.log("MATCH" );
                            resolve(found)
                        }
                        resolve("Not Found")
                    }
                })
            }).catch((err) => {
                reject(err)
            })
        })
    }



var subscriberManager = module.exports = {
    /**
     *@brief adds a Subscriber to the database
     *
     *@return a promise passing the newly added user to the function called back
     */
    addSubscriber: function (_subscriber){
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                dbMan.models.instance.subscriber.findOne({ Email: _subscriber.Email }, function (err, found) {
                    if (err) {
                        reject(err)
                    }
                    if(!found){
                        var newSubscriber = new dbMan.models.instance.subscriber(_subscriber)
                        newSubscriber.save(function (err) {
                            if (err) {
                                logger.error(err)
                                reject(err)
                            } else {
                                logger.debug('Subscriber Added Successfully!')
                                resolve(newSubscriber)
                            }
                        })
                        //resolve(found)

                    }else{
                        //WARNING USER NAME IS TAKEN code goes here
                        logger.debug('ERROR: ' + found.Email + " already exists")
                        reject( found.Email + ' already exists')
                        //resolve(null)
                    }
                });
            }).catch((err)=>{
                reject(err)
            })
        })
    },

    checkSubscriberExists: function (_email) {
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                dbMan.models.instance.subscriber.findOne({ Email: _email }, function (err, found) {
                    if (err || !found) {
                        err = err || 'no email exits'
                        logger.debug(err)
                        resolve(false)
                    } else {
                        logger.debug('Found subscriber : ' + found.Username)
                        resolve(true)
                    }
                })
            }).catch((err) => {
                reject(err)
            })
        })
    },

    /**
     *@brief retrieves a subscriber from the database
     *
     *@return a promise passing the found subscriber back or a null object
     */
    getsubscriber: function (_subscriber) {
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                dbMan.models.instance.subscriber.findOne({ Email: _subscriber.Email }, function (err, found) {
                    if (err || !found) {
                        err = err || 'could not find ' + _subscriber.Email
                        logger.error(err)
                        resolve(null)
                    } else {
                        logger.debug('Found subscriber: ' + found.Email)
                        resolve(found)
                    }
                })
            }).catch((err) => {
                reject(err)
            })
        })
    },
    addToPatientList: function(_subscriber,_newPatient){
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                //Validate email:
                console.log("validating")
                var re = /\S+@\S+\.\S+/;
                if (!re.test(_newPatient)) {
                   // resolve(null); //need to test if this is right
                }
                console.log("valid " )

                //Find 
                dbMan.models.instance.subscriber.findOne({ Email: _subscriber.Email }, function (err, found) {
                    if (err) {
                        logger.error(err)
                        reject(err)
                    }
                    //now update
                    logger.debug('Found subscriber: ' + found.PatientList)
                    
                    //is that subscriber already on the list?
                    var updateValue;
                    if (found.PatientList == null) {
                        updateValue = [_newPatient]
                    } else {
                        updateValue = found.PatientList
                        if (updateValue.indexOf(_newPatient) > -1) {
                            //already on list
                            reject(null);
                            return null;
                        }
                        updateValue.push(_newPatient)
                    }
                    //store updated value
                    console.log(updateValue)


                    //Check if password is correct:
                    validatePatient(_newPatient);





                    var query_object = {Email: _subscriber.Email};
                    var update_values_object = {PatientList: updateValue};
                    dbMan.models.instance.subscriber.update(query_object, update_values_object, null, function(err) {
                        if (err) console.log(err);
                        else console.log('Yuppiie!');
                    });
                    resolve(found)
                })

            }).catch((err) => {
                reject(err)
            })
        })
    },
    removeFromPatientList: function(_subscriber,_oldSubscriber){
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                  //Find 
                dbMan.models.instance.subscriber.findOne({ Email: _subscriber.Email }, function (err, found) {
                    if (err) {
                        logger.error(err)
                        reject(err)
                    }
                    //now remove
                    logger.debug('Found subscriber: ' + found.PatientList)
                    
                    //is that subscriber on the list?
                    var updateValue;
                    if (found.PatientList == null) {
                        resolve("notOnList");
                        return null;
                    } else {
                        updateValue = found.PatientList
                        if (updateValue.indexOf(_oldSubscriber) < -1) {
                            //already on list
                            resolve("notOnList");
                            return null;
                        }
                        updateValue.splice(_oldSubscriber.indexOf(_oldSubscriber),1)
                    }
                    //store updated value
                    console.log(updateValue)
                    var query_object = {Email: _subscriber.Email};
                    var update_values_object = {PatientList: updateValue};
                    dbMan.models.instance.subscriber.update(query_object, update_values_object, null, function(err){
                        if (err) console.log(err);
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
                
                var who = { Email: keys.userEmailDecrypt(key1) }
                dbMan.models.instance.subscriber.findOne(who, function (err, found) {
                    if (err || !found) {
                        logger.error(err || who)
                        reject(err)
                    } else if (found.RegistrationObject.k1 === key1 && found.RegistrationObject.k2 === key2) {
                        dbMan.models.instance.subscriber.update(who, { RegistrationObject: { c: 'registered' } }, function (err) {
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
    }
}