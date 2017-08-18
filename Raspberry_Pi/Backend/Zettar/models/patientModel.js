var util = require('util')
var uuidv1 = require('uuid/v1')

var CryptoJS = require("crypto-js");
const ENCRYPT_KEY = "xP{}Lk.x#3V2S?F2p'q{kqd[Qu{7/S-d*bzt"

function encrypt(value) {
    return CryptoJS.AES.encrypt(value, ENCRYPT_KEY).toString()
}
function decrypt(value) {
    return CryptoJS.AES.decrypt(value, ENCRYPT_KEY).toString(CryptoJS.enc.Utf8)
}


var mailer = require('../lib/mailer')
var keys = require('../lib/keys')
var logger = require('../revaLog')

const userModel = require('./usersModel')
function PatientModel(){
//-------------------------------begin fields---------------------------------//
        this.fields = {
            AccessPassword: {
                type: "text",
                default: function () { return encrypt(uuidv1()) },
                rule: {
                    required: true
                }
            },
            SubscriberList: {
                type: "list",
                typeDef: "<text>"
            },
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
            Address: "text",
            Age: {
                type: "int",
                rule: {
                    validator: function (value) { return value > 0; },
                    message: 'Age must be greater than 0'
                }
            },
            Weight: {
                type: "float",
                rule: {
                    validator: function (value) { return value > 0; },
                    message: 'weight must be greater than 0'
                }
            },
            Height: {
                type: "float",
                rule: {
                    validator: function (value) { return value > 0; },
                    message: 'Hieght must be greater than 0'
                }
            },
            Reason: {
                type: "text",
                rule: {
                    validator: function (value) {
                        var val = value.toLowerCase();
                        if (val == 'age' || val == 'illness' || val == 'disability' || val == 'accident' || val == 'other')
                            return true;
                        return false;
                    },
                    message: 'Reason is not included in the set of reasons.'
                }
            },
            RegistrationObject: {
                type: 'map',
                typeDef: '<text, text>',

                default: function () {
                    const dbMan = require('../databaseManager')
                    var name = this.Username;
                    var context = { c: 'sending', k1: keys.userEmailEncrypt(name), k2: uuidv1() }
                    mailer.mailEmialConfirmationUrl(this.Email, context.k1, context.k2).then(function () {
                        context.c = 'awaiting'
                        dbMan.models.instance.patient.update({ Username: name }, { RegistrationObject: context });
                    }).catch(function (e) {
                        logger.error(e)
                        context.c = 'failed'
                        dbMan.models.instance.patient.update({ Username: name }, { RegistrationObject: context });
                    })
                    return context
                }
            }
        }
//-------------------------------End fields---------------------------------//
        this.methods = {
            verifyPassword: function (value) {
                return decrypt(this.Password) === value
            }
        }
        this.after_save = function (instance, options, next) {
            const dbMan = require('../databaseManager')
            //Encrypt password
            dbMan.models.instance.patient.update(
                { Username: instance.Username },
                { Password: encrypt(instance.Password) },
                function (err) {
                    if (err) {
                        console.log(err); next(err)
                    } else { next() }
                });
        }
   
}
PatientModel.prototype = userModel.class 
var thing = new userModel.class()
var obj = {}

for (var k in thing) {
    obj[k] = thing[k]
}
module.exports = obj
module.exports.class = PatientModel
