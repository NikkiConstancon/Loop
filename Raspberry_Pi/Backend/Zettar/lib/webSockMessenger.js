/**
 * @fileOverview
 * That that will allow bidirectional multiplexed communications between the client and the server.
 * The mediator design pattern is implemented, with the intent is to provide a common platform for
 * message passing where other modules may implement concrete classes to consume and send messages
 * across the global system.
 */

const logger = require('../revaLog')
const server = require('../webServer')


var dbMan = require('../databaseManager');
var PatientManager = require('../patientManager');
var SubscriberManager = require('../subscriberManager');
var uuidv1 = require('uuid/v1')



const WebSocket = require('ws');
const wss = module.exports.wss = server.wss





const CHANNEL_KEY = "|^|"
const META_KEY = "|#|"
var USER_ANONYMOUS = '--ANONYMOUS--'


var requiered = ['connect', 'close', 'sub']

var subServiceOptionsMap = {}

/**
 * @class This class enables sending of messages between  the client and server via a single socket
 * 
 * @brief it is a event emmiter pattern that will emit messages to all corresponding  attached messenger protocols
 */
const webSockMessenger = module.exports = {
    /**
     *@brief attach a series of protocols to be run at client connection, disconnection and messaging
     *
     *@param key {sting} is the key to be bound for all procedure clusters
     *@param options {[function]} is the procedure clusters
     **/
    attach: function (key, options) {
        if (key === undefined || options == undefined) {
            throw new Error('@webSockMessenger#attach: key or options are not defied')
        }
        if (subServiceOptionsMap[key]) {
            throw new Error('@webSockMessenger: key ' + key + ' hase already been attach')
        }
        for (var i in requiered) {
            if (!options[requiered[i]] || !(options[requiered[i]] instanceof Function)) {
                throw new Error('@webSockMessenger#attach: option ' + requiered[i] + ' of key ' + key + ' is missing or not a function')
            }
        }
        subServiceOptionsMap[key] = options
        MetaHandlerMap[key] = new MetaHandler
        return MetaHandlerMap[key];
    },
    getUserSocketContextMap: function () { return userSocketContextMap },
    getUserSocketContext: function (user) { return userSocketContextMap[user] }
}
var userSocketContextMap = {}
var MetaHandlerMap = {}
function MetaHandler() {
    this.metaMap = {}
    this.publisherMap = {}
}
MetaHandler.prototype.newMeta = function (metaKey) {
    if (this.metaMap[metaKey]) {
        return this.metaMap[metaKey]
    }
    this.metaMap[metaKey] = new Meta(metaKey, this)
    return this.metaMap[metaKey]
}
MetaHandler.prototype.bindPublisher = function (metaKey, pub) {
    this.newMeta(metaKey).bindPublisher(pub)
}
MetaHandler.prototype.unbindPublisher = function (metaKey, pub) {
    this.metaMap[metaKey].unbindPublisher(pub)
}

function Meta(metaKey, metaHandler) {
    this.metaKey = metaKey
    this.publisherMap = {}
    this.meta = {}
    this.metaHandler = metaHandler
}
Meta.prototype.bindPublisher = function (pub) {
    this.publisherMap[pub.context.userUid] = { publisher: pub, timeout: undefined };
    this.pullMeta(pub.context.userUid)
}
Meta.prototype.unbindPublisher = function (pub) {
    delete this.publisherMap[pub.context.userUid]
}
Meta.prototype.getMeta = function () {
    return this.meta
}
Meta.prototype.setMeta = function (obj) {
    this.meta = obj
    this.pushMeta()
}
Meta.prototype.updateMeta = function (obj) {
    this.meta = Object.assign(obj, this.meta)
    this.pushMeta()
}
Meta.prototype.pushMeta = function () {
    for (var pupUserUid in this.publisherMap) {
        this.pullMeta(pupUserUid)
    }
}
Meta.prototype.pullMeta = function (pupUserUid) {
    try {
        var map = this.publisherMap[pupUserUid]
        if (map) {
            if (!map.timeout) {
                map.timeout = setTimeout(() => {
                    try {
                        var publisher = this.publisherMap[pupUserUid].publisher
                        publisher.publish({ [META_KEY]: { [this.metaKey]: this.meta } })//, null, true)
                        map.timeout = undefined
                    } catch (e) { }
                }, 128)
            }
        }
    } catch (e) { logger.debug(e) }
}
Meta.prototype.free = function () {
    this.setMeta({})
}


function getGreeting() {
    return JSON.stringify({
        init: { webSockMessenger: 'succsess' }
    })
}


//TODO deviceUid / serviceinstanceuuid is madetory
function parseAuthorizationHeader(header) {
    authField = header.authorization
    return new Promise(function (res, rej) {
        try {
            var auth = authField.split(' ')
            var str = new Buffer(auth[1], 'base64').toString()
            var strs = str.split(':')
            return res({ Username: strs[0], Password: strs[1], serviceInstanceUuid: header.serviceinstanceuuid })
        } catch (e) {
            res({ Username: USER_ANONYMOUS, Password: '', serviceInstanceUuid: header.serviceinstanceuuid, error: e })
        }
    })
}

function authorize(ws) {
    return new Promise(function (res, rej) {
        return parseAuthorizationHeader(ws.upgradeReq.headers).then(function (auth) {
            var userUid = auth.Username;
            var ret = { ws: ws, user: auth.Username, serviceInstanceUuid: auth.serviceInstanceUuid }
            if (auth.Username === '' || auth.Username == USER_ANONYMOUS) {
                ret.user = USER_ANONYMOUS
                res(ret)
            } else {
                return PatientManager.getPatient(auth).then(function (pat) {
                    if (pat.verifyPassword(auth.Password)) {
                        res(ret)
                    } else {
                        ret.user = USER_ANONYMOUS
                        ret.tmpAuthError = { text: "Incorrect passord", field: "password" }
                        res(ret)
                    }
                }).catch(function (e) {
                    //THIS IS NOT HOW USER MANAGEMENT SHOULD WORK !! WE NEED TO RETHINK THE USER'S LAYOUT
                    //THIS WILL ONLY CAUSE ERRORS AND GRIEF DOWN THE ROAD
                    //HONESTLY DON'T KNOW WHY THE INITIAL DESIGN WAS NOT FOLLOWED (MAYBE IT IS JUST ME THAT IS INCAPABLE TO COMPREHEND THE CURRENT LAYOUT)
                    auth.Email = auth.Username
                    return SubscriberManager.getsubscriber(auth).then(function (thisIsSoWrong) {
                        //todo validate password
                        if (thisIsSoWrong.verifyPassword(auth.Password)) {
                            res(ret)
                        } else {
                            ret.user = USER_ANONYMOUS
                            ret.tmpAuthError = { text: "Incorrect passord", field: "password" }
                            res(ret)
                        }
                    }).catch(function (e) {
                        ret.user = USER_ANONYMOUS
                        ret.tmpAuthError = { text: "No such account", field: "userUid" }
                        res(ret)
                    })
                })
            }
        }).catch(function (err) {
            rej(err)
        })
    })
}

function ServicePublisher(context, key, options) {
    this.context = context
    this.enabled = options.defaultEnabled
    this.serviceBound = options.requirePersistentLink//deprected!! (for now)
    this.queue = []
    this.key = key
    this.metaKeys = []
}
ServicePublisher.prototype.publish = function (msg, errcb, force) {
    if (!force && !this.enabled) {
        return
    }
    this.queue.push(msg)
    if (!this.timeOut) {
        this.timeOut = setTimeout( param => {
            pushMessage(this.context.ws, this.key, this.queue, errcb)
            this.queue = []
            this.timeOut = null
        }, 16)//for accumalting data befor sending
    }
}
ServicePublisher.prototype.registerMeta = function (metaKey) {
    MetaHandlerMap[this.key] && MetaHandlerMap[this.key].bindPublisher(metaKey, this)
    this.metaKeys.push(metaKey)
}

ServicePublisher.prototype.deregisterMeta = function () {
    for (var i in this.metaKeys) {
        metaKey = this.metaKeys[i]
        MetaHandlerMap[this.key] && MetaHandlerMap[this.key].unbindPublisher(metaKey, this)
    }
    this.metaKeys = []
}

function pushMessage (ws, key, msg, errcb, context) {
    msgObj = { [key]: msg }
    msg = JSON.stringify(msgObj)
    logger.silly('on-publish', key, msg)
    if (errcb) {
        ws.send(msg, errcb)
    } else {
        ws.send(msg, function (err) {
            if (err) {
                //TODO hadle these errors
                logger.error('@WebSockMessenger#connection', err)
                logger.error('@WebSockMessenger#connection msg:', msg)
                context && context.deleteContext()
            }
        })
    }
}

function UserSocketContext(clientBindingInfo) {
    logger.info("--connected--  userUid=" + this.userUi + ", deviceUid=", this.deviceUid); 
    var ws = clientBindingInfo.ws
    this.userUid = clientBindingInfo.user
    this.deviceUid = clientBindingInfo.serviceInstanceUuid
    this.subServiceMap = {}
    var tmpAuthError = clientBindingInfo.tmpAuthError
    tmpAuthError && (this.tmpAuthError = tmpAuthError)
    if (userSocketContextMap[this.userUid] && userSocketContextMap[this.userUid][this.deviceUid]) {

        var subServiceMap = userSocketContextMap[this.userUid][this.deviceUid].subServiceMap
        for (var key in subServiceMap) {
            subServiceOptionsMap[key].close(userSocketContextMap[this.userUid][this.deviceUid].publishers[key])
        }
       // userSocketContextMap[this.userUid][this.deviceUid].ws.close()
       // delete userSocketContextMap[this.userUid][this.deviceUid]
        userSocketContextMap[this.userUid][this.deviceUid].ws.terminate()
        clientBindingInfo.ws = null
        c = logger.warn('@WebSocketMessenger$userSocketContext:userSocketContextMap duplicate keys', clientBindingInfo)
        clientBindingInfo.ws = ws
        ws.send(JSON.stringify({ RCC: { ERROR: 'DUP_DEVICE_UID' } }))
        ws.close()
        return
    }
    this.ws = ws
    userSocketContextMap[this.userUid] || (userSocketContextMap[this.userUid] = {})
    userSocketContextMap[this.userUid][this.deviceUid] = this;

    this.publishers = {}
    this.rcc_service_bound = false
    for (var key in subServiceOptionsMap) {
        this.subServiceMap[key] = new ServicePublisher(this, key, subServiceOptionsMap[key])
        subServiceOptionsMap[key].connect(this.subServiceMap[key])
    }
    ws.on('message', message => {
        //TODO set publisher to be users socket.send
        logger.debug('on-message', message)
        try {
            var json = JSON.parse(message)
            for (var key in json) {
                var subscriber = subServiceOptionsMap[key].sub
                if (subscriber) {
                    var publisher = this.subServiceMap[key]
                    if (json[key] instanceof Array) {
                        for (var msgNum = 0; msgNum < json[key].length; msgNum++) {
                            subscriber(this.subServiceMap[key], json[key][msgNum]);
                        }
                    } else {
                        subscriber(this.subServiceMap[key], json[key]);
                    }
                } else {
                    logger.warn("@WebSoekcetMessenger#ws.on('message'): lost message! " + key);
                }
            }
        } catch (e) {
            logger.error("@WebSoekcetMessenger#ws.on('message'): ", e)
        }
    });
    ws.on('close', param => {
        try {
            this.deleteContext()
            logger.debug('@WebSockMessenger#close: context= ', this.userUid)
            var subServiceMap = this.subServiceMap
            for (var key in subServiceMap) {
                try {
                    if (!ws.REVA_PREMATURE_CLOSE) {
                        subServiceOptionsMap[key].close(this)
                    }
                } catch (e) {
                    logger.debug(e)
                }
            }
        } catch (e) { logger.error(e) } finally {
            delete userSocketContextMap[this.userUid][this.deviceUid]
        }
    });
}
UserSocketContext.prototype.deleteContext = function () {
    try {
        for (var key in subServiceOptionsMap) {
            this.subServiceMap[key].deregisterMeta()
        }
        delete userSocketContextMap[this.userUid][this.deviceUid]
    } catch (e) { }
}

wss.on('connection', function connection(ws) {
    authorize(ws).then(function (clientBindingInfo) {
        new UserSocketContext(clientBindingInfo)
    }).catch(function (e) {
            if (e.clientSafe) {
                pushMessage(ws, 'RCC', buildError('AUTH', e.clientSafe))
            }
            ws.REVA_PREMATURE_CLOSE  = true
            ws.close()
            e && logger.error('@webSockMessenger#on.connect:', e)
        }).catch(function (e) {
            e && logger.error(e)
        })
})


function buildError(key, errObject) {
    return { ERROR: { [key]: errObject } }
}






var RCC_EXCLUDE_PAUS_RESUME_MAP = { RCC: true, UserManager: true }
webSockMessenger.attach('RCC', {
    connect: function (publisher) {
        publisher.publish({ CONNECTED: { USER_UID: publisher.context.userUid, ERROR: publisher.context.tmpAuthError }, RCC_REDIRECT: ["UserManager"] })
        delete publisher.context.tmpAuthError;
    },
    close: function (publisher) {
    },
    sub: function (publisher, obj) {
        logger.info("RCC", obj)
        try {
            var boundCount = obj.SERVICE_BOUND_COUNT
            if (boundCount !== undefined) {
                if (boundCount <= 1) {
                    publisher.context.rcc_service_bound = false
                } else {
                    publisher.context.rcc_service_bound = true
                }
            }
            if (obj.PAUSE_RESUME) {
                try {
                    var key = obj.PAUSE_RESUME.SERVICE_KEY
                    if (!RCC_EXCLUDE_PAUS_RESUME_MAP[key]) {
                        var servicePlisher = publisher.context.subServiceMap[key]
                        servicePlisher.enabled = Boolean(obj.PAUSE_RESUME.ENABLEMENT).valueOf()
                        for (var i in servicePlisher.metaKeys) {
                            var metaKey = servicePlisher.metaKeys[i]
                            var metaObj = MetaHandlerMap[servicePlisher.key].metaMap[metaKey]
                            for (var i in servicePlisher.metaKeys) {
                                metaObj && metaObj.pullMeta(servicePlisher.metaKeys[i])
                            }
                        }
                    }
                } catch (e) {
                    logger.error('WebSocketMessenger$RCC#obj.PAUS_RESUME: ', e)
                }
            }
            if (obj.SERVICE_BINDING) {
                try {
                    var key = obj.SERVICE_BINDING.SERVICE_KEY

                    if (subServiceOptionsMap[publisher.key].requirePersistentLink) {
                        publisher.context.subServiceMap[key].serviceBound = true
                    } else {
                        publisher.context.subServiceMap[key].serviceBound = Boolean(obj.PAUSE_RESUME.ENABLEMENT).valueOf()
                    }
                } catch (e) {
                    logger.error('WebSocketMessenger$RCC#obj.SERVICE_BINDING: ', e)
                }
            }
        } catch (e) {
            logger.error(e)
        }
    },
    requirePersistentLink: true,
    defaultEnabled: true
})



function DispatchChannels(handlers, publisher, rootMsg, channeler) {
    var obj = rootMsg[CHANNEL_KEY]
    if (obj) {
        try {
            for (var key in obj) {
                for (var id in obj[key]) {

                    function channeler(msg, errcb) {
                        publisher.publish({ [CHANNEL_KEY]: { [id]: msg } }, errcb)
                    }
                    var msg = obj[key][id]
                    handlers[key](publisher, msg, key, channeler)
                }
            }
        } catch (e) {
            logger.debug(e, obj)
        }
    }
}

function DispatchMessages(handlers, publisher, rootMsg, channeler) {
    try {
        for (var key in rootMsg) {
            var msg = rootMsg[key]
            handlers[key](publisher, msg, key, channeler)
        }
    } catch (e) {
        logger.debug(e, rootMsg)
    }
}



var userManagerChannels = {
    REGISTER: function (publisher, msg, key, channeler) {
        DispatchMessages({

            VALIDATE_EMAIL: function (publisher, msg, key) {
                channeler({ PASS: true })
                //channeler({ PASS: false, ERROR: "This email address is not available" })
            },
            REGISTER_PATIENT: function (publisher, msg, key) {
                var obj = msg;
                //TODO: Move to patiantManager
                function deserialize(obj) {
                    var test = require('../models/patientModel').fields
                    for (var key in test) {
                        switch (test[key].type) {
                            case 'int': { obj[key] && (obj[key] = parseInt(obj[key])) } break
                            case 'float': { obj[key] && (obj[key] = parseFloat(obj[key])) } break
                        }
                    }
                }
                deserialize(obj)
                PatientManager.getPatient(obj)
                    .then(function () {
                        channeler({ PATIENT_ERROR: 'This username has been taken', PATIENT_PASS: false })
                    }).catch(function () {
                        return PatientManager.addPatient(obj).then(function (pat) {
                            channeler({ PATIENT_PASS: true })
                        }).catch(function (e) {
                            channeler({ PATIENT_ERROR: e.message || e, PATIENT_PASS: false })
                        })
                    }).catch(function (e) {
                        logger.error('@webSockMessenger$UserManager#sub:KEY_REGISTER_USER', e)
                        channeler({ PATIENT_ERROR: 'something went wrong', PATIENT_PASS: false })
                    })
            },
            REGISTER_NON_PATIENT: function (publisher, msg, key) {
                var obj = msg;
                //TODO: Move to patiantManager
                function deserialize(obj) {
                    var test = require('../models/patientModel').fields
                    for (var key in test) {
                        switch (test[key].type) {
                            case 'int': { obj[key] && (obj[key] = parseInt(obj[key])) } break
                            case 'float': { obj[key] && (obj[key] = parseFloat(obj[key])) } break
                        }
                    }
                }
                deserialize(obj)
                SubscriberManager.getsubscriber(obj)
                    .then(function () {
                        //ugly prot i.e. copy and paste mostly form patient
                        channeler({ NON_PATIENT_ERROR: 'This email has been taken', NON_PATIENT_PASS: false })
                    }).catch(function () {
                        return SubscriberManager.addSubscriber(obj).then(function (pat) {
                            channeler({ NON_PATIENT_PASS: true })
                        }).catch(function (e) {
                            channeler({ NON_PATIENT_ERROR: e.message || e, NON_PATIENT_PASS: false })
                        })
                    }).catch(function (e) {
                        logger.error('@webSockMessenger$UserManager#sub:KEY_REGISTER_USER', e)
                        channeler({ NON_PATIENT_ERROR: 'something went wrong', NON_PATIENT_PASS: false })
                    })
            }
        }, publisher, msg)
    }
}

webSockMessenger.attach('UserManager', {
    connect: function (publisher) {
        //publisher.publish("--HELLO-- ");
    },
    close: function (publisher) {
    },
    sub: function (publisher, obj) {
        DispatchChannels(userManagerChannels, publisher, obj)

        /*
        if (obj.TEST_EMAIL_AVAILABLE) {
            //obj.TEST_EMAIL_AVAILABLE = 'This email address is not available'
            obj.TEST_EMAIL_AVAILABLE = ''
            publisher.publish(obj);
        } else if (obj.KEY_REGISTER_USER) {
            delete obj.KEY_REGISTER_USER
            //TODO: Move to patiantManager
            function deserialize(obj) {
                var test = require('../models/patientModel').fields
                for (var key in test) {
                    switch (test[key].type) {
                        case 'int': { obj[key] && (obj[key] = parseInt(obj[key])) } break
                        case 'float': { obj[key] && (obj[key] = parseFloat(obj[key])) } break
                    }
                }
            }
            deserialize(obj)
            PatientManager.getPatient(obj)
                .then(function () {
                    publisher.publish({ KEY_REGISTER_USER: { Username: 'This username has been taken' } })
                }).catch(function () {
                    return PatientManager.addPatient(obj).then(function (pat) {
                        publisher.publish({ KEY_REGISTER_USER: true })
                    }).catch(function (e) {
                        publisher.publish({ KEY_REGISTER_USER: { ERROR: e.message || e } })
                    })
                }).catch(function (e) {
                    logger.error('@webSockMessenger$UserManager#sub:KEY_REGISTER_USER', e)
                    publisher.publish({ KEY_REGISTER_USER: { ERROR: 'something went wrong' } })
                })
        }
        logger.info(obj);
        */
    },
    requirePersistentLink: true,
    defaultEnabled: true
})
