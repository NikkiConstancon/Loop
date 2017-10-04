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


var requiered = ['connect', 'close', 'receiver']

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
        if (options.subListUpdater) {
            PublisherHandlerMap[key] = new PublisherHandler(key, options.subListUpdater)
            return PublisherHandlerMap[key]
        } else {
            return null
        }
    },
    transmitTo: function (serviceKey, userUid, msg, errcb) {
        for (var deviceUid in userSocketContextMap[userUid]) {
            var context = userSocketContextMap[userUid][deviceUid]
            var transmitter = context.subServiceMap[serviceKey]
            transmitter.transmit(msg, errcb)
        }
    }
}
var userSocketContextMap = {}
var PublisherHandlerMap = {}

function PublisherHandler(serviceKey, subListUpdater) {
    this.serviceKey = serviceKey
    this.publisherMap = {}
    this.subListUpdater = subListUpdater
}
PublisherHandler.prototype.getPublisher = function (publisherUsername) {
    if (!this.publisherMap[publisherUsername]) {
        this.publisherMap[publisherUsername] = new Publisher(this, publisherUsername)
    }
    return this.publisherMap[publisherUsername]
}

function Publisher(PublisherHandler, publisherUsername) {
    this.publisherHandler = PublisherHandler
    this.publisherUsername = publisherUsername
    this.subscriberList = []
    this.subscriberTransmitterMap = {}
    this.meta = new Meta(this)
    try {
        this.publisherHandler.subListUpdater(this.publisherUsername, subList => {
            this.subscriberList = subList
        });
    } catch (e) { logger.error(e) }
}
Publisher.prototype.publish = function (msg, errcb) {
    try {
        for (var i in this.subscriberList) {
            var subUid = this.subscriberList[i]
            for (var deviceUid in userSocketContextMap[subUid]) {
                var context = userSocketContextMap[subUid][deviceUid]
                var transmitter = context.subServiceMap[this.publisherHandler.serviceKey]
                transmitter.transmit(msg, errcb)
                if (transmitter.lastMetaId != this.meta.metaSetId && transmitter.transmit(this.meta.obj, errcb)) {
                    transmitter.lastMetaId = this.meta.metaSetId
                }
            }
        }
    } catch (e) { logger.error(e) }
}
Publisher.prototype.setMeta = function (obj) {
    this.meta.setMeta(obj)
}
Publisher.prototype.setMetaField = function (key, value) {
    this.meta.setFiled(key, value)
}
Publisher.prototype.getUsername = function () {
    return this.publisherUsername
}



function Meta(publisher) {
    this.metaSetId = 0;
    this.obj = {}
    this.publisher = publisher
}
Meta.prototype.formatMsg = function(obj){
    this.obj = { [META_KEY]: { [this.publisher.getUsername()]: obj } }
    return this.obj 
}
Meta.prototype.setMeta = function (obj) {
    this.metaSetId++
    this.formatMsg(obj);
    this.publisher.publish(this.obj)
}
Meta.prototype.setFiled = function (key, value) {
    this.metaSetId++
    var map = this.obj
    if (!map[META_KEY]) {
        map[META_KEY] = {}
        map = this.obj[META_KEY]
        if (!map[this.publisher.getUsername()]) {
            map[this.publisher.getUsername()] = {}
        }
    }
    this.obj[META_KEY][this.publisher.getUsername()][key] = value
    this.publisher.publish(this.obj)
}



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
            if (auth.Username == '' || auth.Username == USER_ANONYMOUS) {
                ret.user = USER_ANONYMOUS
                res(ret)
            } else {
                return PatientManager.getPatient(auth).then(function (pat) {
                    if (pat.verifyPassword(auth.Password)) {
                        ret.userType = 'patient'//why should this module ceep track of who is what kind of user? there are so much that is shared by the two principle users they should have been managed as one accout as I intally proposed!
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
                        if (thisIsSoWrong.verifyPassword(auth.Password)) {
                            ret.userType = 'subscriber'//why should this module ceep track of who is what kind of user? there are so much that is shared by the two principle users they should have been managed as one accout as I intally proposed!
                            res(ret)
                        } else {
                            ret.user = USER_ANONYMOUS
                            ret.tmpAuthError = { text: "Incorrect passord", field: "password" }
                            res(ret)
                        }
                    }).catch(function (e) {
                        ret.user = USER_ANONYMOUS
                        ret.tmpAuthError = { text: "No such account. Note that capitalization is important.", field: "userUid" }
                        res(ret)
                    })
                })
            }
        }).catch(function (err) {
            rej(err)
        })
    })
}

function ServiceTransmitter(context, key, options) {
    this.context = context
    this.enabled = options.defaultEnabled
    this.serviceBound = options.requirePersistentLink//deprected!! (for now)
    this.queue = []
    this.key = key
    this.lastMetaId = -1
}
ServiceTransmitter.prototype.transmit = function (msg, errcb, force) {
    if (!force && !this.enabled) {
        return false
    }
    this.queue.push(msg)
    if (!this.timeOut) {
        this.timeOut = setTimeout( param => {
            pushMessage(this.context.ws, this.key, this.queue, errcb)
            this.queue = []
            this.timeOut = null
        }, 16)//for accumalting data befor sending
    }
    return true
}
ServiceTransmitter.prototype.getUserUid = function () { return this.context.userUid }
ServiceTransmitter.prototype.getUserType = function () { return this.context.userType }

function pushMessage (ws, key, msg, errcb, context) {
    msgObj = { [key]: msg }
    msg = JSON.stringify(msgObj)
    logger.silly('on-transmit', key, msg)
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
    this.userType = clientBindingInfo.userType
    var tmpAuthError = clientBindingInfo.tmpAuthError
    tmpAuthError && (this.tmpAuthError = tmpAuthError)
    if (userSocketContextMap[this.userUid] && userSocketContextMap[this.userUid][this.deviceUid]) {

        var subServiceMap = userSocketContextMap[this.userUid][this.deviceUid].subServiceMap
        for (var key in subServiceMap) {
            subServiceOptionsMap[key].close(userSocketContextMap[this.userUid][this.deviceUid].transmitters[key])
        }
       // userSocketContextMap[this.userUid][this.deviceUid].ws.close()
       // delete userSocketContextMap[this.userUid][this.deviceUid]
        userSocketContextMap[this.userUid][this.deviceUid].ws.terminate()
        clientBindingInfo.ws = null
        c = logger.warn('@WebSocketMessenger$userSocketContext:userSocketContextMap duplicate keys', clientBindingInfo)
        clientBindingInfo.ws = ws
        ws.send(JSON.stringify({ RCC: { ERROR: 'DUP_DEVICE_UID' } }))
        setTimeout(()=>ws.close(), 2000)
        return
    }
    this.ws = ws
    userSocketContextMap[this.userUid] || (userSocketContextMap[this.userUid] = {})
    userSocketContextMap[this.userUid][this.deviceUid] = this;

    this.transmitters = {}
    this.rcc_service_bound = false
    for (var key in subServiceOptionsMap) {
        var transmitter = new ServiceTransmitter(this, key, subServiceOptionsMap[key])
        this.subServiceMap[key] = transmitter
        subServiceOptionsMap[key].connect(transmitter)
    }
    ws.on('message', message => {
        //TODO set transmitter to be users socket.send
        logger.debug('on-message', message)
        try {
            var json = JSON.parse(message)
            for (var key in json) {
                var subscriber = subServiceOptionsMap[key].receiver
                if (subscriber) {
                    var transmitter = this.subServiceMap[key]
                    if (json[key] instanceof Array) {
                        for (var msgNum = 0; msgNum < json[key].length; msgNum++) {
                            if (subServiceOptionsMap[key].channels instanceof Object && json[key][msgNum][CHANNEL_KEY]) {
                                DispatchChannels(subServiceOptionsMap[key].channels, this.subServiceMap[key], json[key][msgNum])
                            } else {
                                subscriber(this.subServiceMap[key], json[key][msgNum]);
                            }
                        }
                    } else {
                        throw("Server cannot prase outterm essage ojects that is not in an array on: " + message)
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

function DispatchChannels(channel, transmitter, rootMsg, channeler) {
    var obj = rootMsg[CHANNEL_KEY]
    if (obj) {
        try {
            for (var key in obj) {
                for (var id in obj[key]) {

                    function channeler(msg, errcb) {
                        transmitter.transmit({ [CHANNEL_KEY]: { [id]: msg } }, errcb)
                    }
                    var msg = obj[key][id]
                    try {
                        DispatchChannelKeys(channel[key], transmitter, msg, channeler)
                    }catch(e){
                        logger.error("invalid channel or key with: " + key + "\n", e)
                    }
                }
            }
        } catch (e) {
            logger.debug(e, obj)
        }
    }
}

function DispatchChannelKeys(handlers, transmitter, rootMsg, channeler) {
    for (var key in rootMsg) {
        var msg = rootMsg[key]
        handlers[key](transmitter, msg, key, channeler)
    }
}





var RCC_EXCLUDE_PAUS_RESUME_MAP = { RCC: true, UserManager: true }
webSockMessenger.attach('RCC', {
    connect: function (transmitter) {
        transmitter.transmit({ CONNECTED: { USER_TYPE: transmitter.getUserType(), USER_UID: transmitter.context.userUid, ERROR: transmitter.context.tmpAuthError }, RCC_REDIRECT: ["UserManager"] })
        delete transmitter.context.tmpAuthError;
    },
    close: function (transmitter) {
    },
    receiver: function (transmitter, obj) {
        logger.info("RCC", obj)
        try {
            var boundCount = obj.SERVICE_BOUND_COUNT
            if (boundCount !== undefined) {
                if (boundCount <= 1) {
                    transmitter.context.rcc_service_bound = false
                } else {
                    transmitter.context.rcc_service_bound = true
                }
            }
            if (obj.PAUSE_RESUME) {
                try {
                    var key = obj.PAUSE_RESUME.SERVICE_KEY
                    if (!RCC_EXCLUDE_PAUS_RESUME_MAP[key]) {
                        var serviceTransmitter = transmitter.context.subServiceMap[key]
                        serviceTransmitter.enabled = Boolean(obj.PAUSE_RESUME.ENABLEMENT).valueOf()
                        serviceTransmitter.lastMetaId = -1
                    }
                } catch (e) {
                    logger.error('WebSocketMessenger$RCC#obj.PAUS_RESUME: ', e)
                }
            }
            var key
            if (obj.SERVICE_BINDING) {
                try {
                    key = obj.SERVICE_BINDING.SERVICE_KEY

                    if (subServiceOptionsMap[transmitter.key].requirePersistentLink) {
                        transmitter.context.subServiceMap[key].serviceBound = true
                    } else {
                        transmitter.context.subServiceMap[key].serviceBound = Boolean(obj.PAUSE_RESUME.ENABLEMENT).valueOf()
                    }
                    if (transmitter.context.subServiceMap[key].serviceBound){
                        var serviceTransmitter = transmitter.context.subServiceMap[key]
                        subServiceOptionsMap[key].onEnable && subServiceOptionsMap[key].onEnable(serviceTransmitter);
                    }
                } catch (e) {
                    logger.error('WebSocketMessenger$RCC#obj.SERVICE_BINDING: on key: ' + key + '\n', e)
                }
            }
        } catch (e) {
            logger.error(e)
        }
    },
    requirePersistentLink: true,
    defaultEnabled: true
})




