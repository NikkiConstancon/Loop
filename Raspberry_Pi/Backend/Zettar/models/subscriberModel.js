var util = require('util')
var uuidv1 = require('uuid/v1')

var Model = require('../lib/modelClass')

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

class SubscriberModel extends Model {
	constructor() {
	    super()
	    this.fields = {
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
            PublisherBindingConfirmationMap: {
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
	    },
	    this.key = ["Email"]
		this.methods = {
		    verifyPassword: function (value) {
		        return decrypt(this.Password) === value
		    }
		}
		this.after_save = function (instance, options, next) {
		    const dbMan = require('../databaseManager')
		    //Encrypt password
		    dbMan.models.instance.patient.update(
		        { Username: instance.Email },
		        { Password: encrypt(instance.Password) },
		        function (err) {
		            if (err) {
		                console.log(err); next(err)
		            } else { next() }
		        });
		}
	}
}
var thing = new SubscriberModel()
var obj = {}

for (var k in thing) {
    obj[k] = thing[k]
}
module.exports = obj