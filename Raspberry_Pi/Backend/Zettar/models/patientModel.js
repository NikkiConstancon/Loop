/**
 * @file
 * Cassandra database model that describes the Patient schema
 * It also defines some methods to pe performed  on patients
 **/


var util = require('util')
var uuidv1 = require('uuid/v1')

const userManagerUtil = require('../userManagerUtil')

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

var Model = require('../lib/modelClass')
module.exports = {
//-------------------------------begin fields---------------------------------//
        fields: {
            Username: 'text',
            Password: {
                type: "text",
                rule: {
                    required: true
                }
            },
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
            ZettaletUuid: {
                type: 'text'
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
            PubSubBindingConfirmationMap: {
                type: 'map',
                typeDef: '<text, text>'
            },
            RegistrationObject: {
                type: 'map',
                typeDef: '<text, text>',

                default: function () {
                    const dbMan = require('../databaseManager')
                    var name = this.Username;
                    var context = { c: 'sending', k1: keys.userEmailEncrypt(name), k2: uuidv1(), k3: uuidv1() }
                    mailer.mailEmialConfirmationUrl(this.Email, context.k1, context.k2, context.k3, name).then(function () {
                        context.c = 'awaiting'
                        dbMan.models.instance.patient.update({ Username: name }, { RegistrationObject: context });
                    }).catch(function (e) {
                        logger.error(e)
                        context.c = 'failed'
                        dbMan.models.instance.patient.update({ Username: name }, { RegistrationObject: context });
                    })
                    return context
                }
            },
            DeviceMap: {
                type: 'map',
                typeDef: '<text, boolean>',
                default: function () { return {} }
            },
            FlagPasswordEncrypted: {
                type: 'boolean'
            }
        },
//-------------------------------End fields---------------------------------//
        key : ["Username"],
        methods: {
            verifyPassword: function (value) {
                return decrypt(this.Password) === value
            },
            getPassword: function () { return this.Password },
            connectDevice: function (deviceKey) {
                connectDeviceStore[this.Email] || (connectDeviceStore[this.Email] = {})
                //concurrency problem fix
                if (!connectDeviceStore[this.Email].timeout) {
                    connectDeviceStore[this.Email].map = {}
                    connectDeviceStore[this.Email].timeout = setTimeout(() => {
                        dbMan.models.instance.patient.update({ Username: this.Username }, { DeviceMap: Object.assign(this.DeviceMap || {}, connectDeviceStore[this.Email].map) })
                        delete connectDeviceStore[this.Email]
                    }, 512)
                }
                connectDeviceStore[this.Email].map[deviceKey] = true
            },
            disconnectAllDevices: function () {
                for (var key in this.DeviceMap) {
                    this.DeviceMap[key] = false;
                }
                return dbMan.models.instance.patient.update({ Username: this.Username }, { DeviceMap: this.DeviceMap }, null, function (err) {
                    if (err) {
                        logger.error("@PateintModel$connectDevice: " + err)
                    }
                })
            },
            getSubscriberList: function () {
                var ret = [this.Username]
                //concat not working?
                for (var i in this.SubscriberList) {
                    ret.push(this.SubscriberList[i])
                }
                return ret
            },
            addPubSubRequestAsRequester: userManagerUtil.addPubSubRequestAsRequester,
            addPubSubRequestAsTarget: userManagerUtil.addPubSubRequestAsTarget,
            pubSubRequestOnDecision: userManagerUtil.pubSubRequestOnDecision
        },
        before_save: function (instance, options, next) {
            //instance.Password = encrypt(instance.Password)
            next()
        },
        after_save: function (instance, options, next) {
            const dbMan = require('../databaseManager')
            //Encrypt password
            //Why is express cassandra so shitty? nothing is working as expected
            if (!instance.FlagPasswordEncrypted) {
                dbMan.models.instance.patient.update(
                    { Username: instance.Username },
                    { Password: encrypt(instance.Password), FlagPasswordEncrypted: true },
                    function (err) {
                        if (err) {
                            console.log(err); next(err)
                        } else { next() }
                    });
            } else {
                next()
            }

        }
    }
/*
var thing = new PatientModel()
var obj = {}

for (var k in thing) {
    obj[k] = thing[k]
}
module.exports = obj
module.exports.class = PatientModel
*/

var dbMan;
setTimeout(function () { dbMan = require('../databaseManager') }, 0)
var connectDeviceStore = {}//concurrency problem fix @ connectDevice()
