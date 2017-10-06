var util = require('util')
var uuidv1 = require('uuid/v1')


const pubSubBindingService = require('../services/PubSubBindingService')
var Model = require('../lib/modelClass')
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
            SubscriberList: {
                type: "list",
                typeDef: "<text>"
            },
	        PatientList     : {
	                type: "list",
	                typeDef: "<text>"
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
                    var name = this.Email;
                    var context = { c: 'sending', k1: keys.userEmailEncrypt(name), k2: uuidv1() }
                    //TODO fix this
                    //mailer.mailEmialConfirmationUrl(this.Email, context.k1, context.k2).then(function () {
                    //    context.c = 'awaiting'
                    //    dbMan.models.instance.subscriber.update({ : name }, { RegistrationObject: context });
                    //}).catch(function (e) {
                    //    logger.error(e)
                    //    context.c = 'failed'
                    //    dbMan.models.instance.subscriber.update({ Username: name }, { RegistrationObject: context });
                    //})

                    return context
                }
            },
            FlagPasswordEncrypted: {
                type: 'boolean'
            }
	    },
	    key: ["Email"],
        methods: {
		    verifyPassword: function (value) {
		        return decrypt(this.Password) === value
            },
            getPassword: function () { return this.Password },
            addPubSubRequestAsRequester: userManagerUtil.addPubSubRequestAsRequester,
            addPubSubRequestAsTarget: userManagerUtil.addPubSubRequestAsTarget,
            addToSubscriberList: userManagerUtil.addToSubscriberList,
            addToPatientList: userManagerUtil.addToPatientList,
            removeFromSubscriberList: userManagerUtil.removeFromSubscriberList,
            removeFromPatientList: userManagerUtil.removeFromPatientList,
            pubSubRequestOnDecision: userManagerUtil.pubSubRequestOnDecision,
            getPassword: function () { return this.Password },
            getType: function () { return userManagerUtil.enum.userType.subscriber }
        },
        after_save: function (instance, options, next) {
            const dbMan = require('../databaseManager')
            //Why is express cassandra so shitty? nothing is working as expected
            if (!instance.FlagPasswordEncrypted) {
                dbMan.models.instance.subscriber.update(
                    { Email: instance.Email },
                    { Password: encrypt(instance.Password), FlagPasswordEncrypted: true },
                    function (err) {
                        if (err) {
                            console.log(err); next(err)
                        } else { next() }
                    });
            } else {
                pubSubBindingService.update(instance.Email, instance.PubSubBindingConfirmationMap, instance.PatientList, instance.SubscriberList)
                next()
            }
            
		}
	}