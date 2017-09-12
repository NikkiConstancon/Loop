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
var uuidv1 = require('uuid/v1')



const WebSocket = require('ws');
const wss = module.exports.wss = server.wss


const patientManager = require('../patientManager')




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
    },
    getUserSocketContextMap: function () { return userSocketContextMap },
    getUserSocketContext: function (user) { return userSocketContextMap[user] }
}
var userSocketContextMap = {}




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
            res({ Username: USER_ANONYMOUS, Password: '', serviceInstanceUuid: header.serviceinstanceuuid })
        }
    })
}

function authorize(ws) {
    return new Promise(function (res, rej) {
        return parseAuthorizationHeader(ws.upgradeReq.headers).then(function (auth) {
            var ret = { ws: ws, user: auth.Username, serviceInstanceUuid: auth.serviceInstanceUuid }
            if (auth.Username === '') {
                ret.user = USER_ANONYMOUS
                res(ret)
            } else {
                return patientManager.getPatient(auth).then(function (pat) {
                    if (pat.verifyPassword(auth.Password)) {
                        res(ret)
                    } else {
                        rej(('webSockMessenger$authorizeAsync --- TODO ---  invalid password'))
                    }
                }).catch(function (e) {
                    ret.user = USER_ANONYMOUS
                    res(ret)
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
}
ServicePublisher.prototype.publish = function (msg, errcb) {
    if (!this.enabled) {
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

function pushMessage (ws, key, msg, errcb, context) {
    msgObj = { [key]: msg }
    msg = JSON.stringify(msgObj)
    logger.debug('on-publish', key, msg)
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
    var ws = clientBindingInfo.ws
    this.userUid = clientBindingInfo.user
    this.deviceUid = clientBindingInfo.serviceInstanceUuid
    this.subServiceMap = {}
    if (userSocketContextMap[this.userUid] && userSocketContextMap[this.userUid][this.deviceUid]) {
        c = logger.warn('@WebSocketMessenger$userSocketContext:userSocketContextMap duplicate keys', clientBindingInfo)
        ws.send(JSON.stringify({ RCC: { ERROR: 'device already in use. Close the current connection first' } }))
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
        this.deleteContext()
        logger.debug('@WebSockMessenger#close: context= ', this.userUid)
        var subServiceMap = this.subServiceMap
        for (var key in subServiceMap) {
            subServiceOptionsMap[key].close(this)
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
        publisher.publish({ CONNECTED: { USER_UID: publisher.context.userUid } })
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
                        publisher.context.subServiceMap[key].enabled = Boolean(obj.PAUSE_RESUME.ENABLEMENT).valueOf()
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



webSockMessenger.attach('UserManager', {
    connect: function (publisher) {
        //publisher.publish("--HELLO-- ");
    },
    close: function (publisher) {
    },
    sub: function (publisher, obj) {
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
    },
    requirePersistentLink: true,
    defaultEnabled: true
})

webSockMessenger.attach('Pulse', {
    connect: function (publisher) {
        var count = 0;
        publisher.ival = setInterval(function () {
            publisher.publish(count++, function (err) {
                err && clearInterval(publisher.ival)
            })
        }, 5000)
    },
    close: function (publisher) {
        clearInterval(publisher.ival)
    },
    sub: function (publisher, obj) {
        publisher.publish(obj)
    },
    requirePersistentLink: true,
    defaultEnabled: true
})

/*
    //keys as userId and values as {deviceUuid: userSocketContext}
var userSocketContextMap = {}


function parseError(key, error, message) {
    var obj = {}
    error && (obj.server = error)
    message && (obj.client = message)
    return JSON.stringify({
        error: { [key]: obj }
    })
}

function getGreeting() {
    return JSON.stringify({
        init: { webSockMessenger: 'succsess' }
    })
}

function parseAuthorizationHeader(header) {
    authField = header.authorization
    
    return new Promise(function (res, rej) {
        if (!authField) {
            //return rej(parseError('webSockMessenger', 'authorization header not set, though it is required'))
            res({ Username: '--ANONYMOUS--', Password: '', serviceInstanceUuid: header.serviceinstanceuuid })
        }
        try {
            var auth = authField.split(' ')
            var str = new Buffer(auth[1], 'base64').toString()
            var strs = str.split(':')
            return res({ Username: strs[0], Password: strs[1], serviceInstanceUuid: header.serviceinstanceuuid })
        } catch (e) {
            return rej(parseError(parseError('webSockMessenger', e)))
        }
    })
}

function authorizeAsync(ws) {
    return new Promise(function (res, rej) {
        return parseAuthorizationHeader(ws.upgradeReq.headers).then(function (auth) {
            var ret = { ws: ws, user: auth.Username, serviceInstanceUuid: auth.serviceInstanceUuid }
            if (auth.Username === "--ANONYMOUS--") {
                return res(ret);
            } else {
                return patientManager.getPatient(auth).then(function (pat) {
                    if (pat.verifyPassword(auth.Password)) {
                        return res(ret)
                    } else {
                        rej(parseError('webSockMessenger', 'invalid password'))
                    }
                }).catch(function (e) {
                    return rej(parseError('webSockMessenger', e))
                })
            }
        }).catch(function (err) {
            return rej(err)
        })
    }).catch(function (err) {
        return rej(err)
    })
}

//Constructor
function ServicePublisher(context, serviceKey, userId, ws, connectUid) {
    this.connectUid
    this.key = serviceKey
    this.user = userId
    this.userId = userId
    this.queue = []
    this.timeOut = null
    this.ws = ws
    this.enabled = enablementMap[this.key]
    this.context = context
}
ServicePublisher.prototype.publish = function (msg, errcb) {
    if (!this.enabled || (!this.ws.rcc_service_bound && !requirePersistentLinkMap[this.key])) {
        return
    }
    this.queue.push(msg)
    if (!this.timeOut) {
        this.timeOut = setTimeout(function () {
            this.pushMessage(this.queue, errcb)
            this.queue = []
            this.timeOut = null
        }, 16)//for accumalting data befor sending
    }
}

ServicePublisher.prototype.pushMessage = function (msg, errcb) {
    var errcb = errcb
    var key = this.key
    var ws = this.ws
    msgObj = { [key]: msg }
    msg = JSON.stringify(msgObj)
    logger.debug('on-publish', key, msg)
    if (errcb) {
        ws.send(msg, errcb)
    } else {
        ws.send(msg, function (err) {
            if (err) {
                //TODO hadle these errors
                logger.error('@WebSockMessenger#connection', err)
                logger.error('@WebSockMessenger#connection msg:', msg)
                try {
                    delete userSocketContextMap[this.user][this.connectUid]
                } catch (e) {
                    logger.error('@WebSockMessenger#connection:delete', e)
                }
                ws.close()
            }
        })
    }
}


function userSocketContext(param) {
    if (userSocketContextMap[param.user] && userSocketContextMap[param.user][param.serviceInstanceUuid]) {
        c = logger.warn('@WebSocketMessenger$userSocketContext:userSocketContextMap duplicate keys', param)
        ws.send('serviceInstanceUuid in use , closing [' + param.serviceInstanceUuid + ']')
        ws.close()
        return
    }
    userSocketContextMap[param.user] || (userSocketContextMap[param.user] = {})
    var connectUid = uuidv1();
    userSocketContextMap[param.user][connectUid] = this;

    var ws = param.ws
    var user = param.user
    var publisherMap = {}

    this.publisherMap = publisherMap
    ws.rcc_service_bound = false//deactivate moste servecies to stop thrashing the connection if app not in focus. NOTE! ws is shared
    ws.send(getGreeting(user), function (err) {
        if (err) { logger.error('@WebSockMessenger', err) }
    })
    
    for (var key in connecttors) {
        publisherMap[key] = new ServicePublisher(this, key, user, ws, connectUid)
        connecttors[key](publisherMap[key])
    }
    ws.on('message', function (message) {
        //TODO set publisher to be users socket.send
        logger.debug('on-message', message)
        try {
            var json = JSON.parse(message)
            for (var k in json) {
                if (subscribers[k]) {
                    var publisher = publisherMap[k].publisher
                    if (json[k] instanceof Array) {
                        for (var msgNum = 0; msgNum < json[k].length; msgNum++) {
                            subscribers[k](publisher, json[k][msgNum]);
                        }
                    } else {
                        subscribers[k](publisher, json[k]);
                    }
                } else {
                    logger.warn("@WebSoekcetMessenger#ws.on('message'): lost message! " + k);
                }
            }
        } catch (e) {
            var errMsg = JSON.stringify({
                error: { msg: message, err: e.message }
            })
            logger.error(errMsg)
            ws.send(errMsg)
        }
    });
    ws.on('close', function close() {
        try {
            delete userSocketContextMap[param.user][connectUid]
        } catch (e){ }
        logger.debug('@WebSockMessenger#close: user=' + user)
        for (var key in closeors) {
        }
    });
}


wss.on('connection', function connection(ws) {
    authorizeAsync(ws).then(function (param) {
        new userSocketContext(param)
    }).catch(function (e) {
        ws.send(e)
        ws.close()
        e && logger.error(e)
    })

})




webSockMessenger.attach('RCC', {
    connect: function (publisher) {
    },
    close: function (publisher) {
        clearInterval(publisher.ival)
    },
    sub: function (publisher, obj) {
        var boundCount = obj.SERVICE_BOUND_COUNT
        if (boundCount !== undefined) {
            if (boundCount == 0) {
                publisher.ws.rcc_service_bound = false
            } else {
                publisher.ws.rcc_service_bound = true
            }
        }
        if (obj.PAUSE_SERVICE) {

        }
        if (obj.RESUME_SERVICE) {

        }
    },
    requirePersistentLink: true,
    defaultEnabled: true
})

webSockMessenger.attach('UserManager', {
    connect: function (publisher) {
        publisher.publish("--HELLO-- " + publisher.user);
    },
    close: function (publisher) {
    },
    sub: function (publisher, obj) {
        if (obj.TEST_EMAIL_AVAILABLE) {
            obj.TEST_EMAIL_AVAILABLE = 'This email address is not available'
            //obj.TEST_EMAIL_AVAILABLE = ''
            publisher.publish(obj);
        }else if (obj.KEY_REGISTER_USER) {
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
                        publisher.publish({ KEY_REGISTER_USER: { ERROR: e.message || e }})
                    })
                }).catch(function (e) {
                    logger.error('@webSockMessenger$UserManager#sub:KEY_REGISTER_USER', e)
                    publisher.publish({ KEY_REGISTER_USER: { ERROR: 'something went wrong'}})
                })
        }
        logger.info(obj);
    },
    requirePersistentLink: true,
    defaultEnabled: true
})




webSockMessenger.attach('Pulse', {
    connect: function (publisher) {
        var count = 0;
        publisher.ival = setInterval(function () {
            publisher.publish(count++, function (err) {
                err && clearInterval(publisher.ival)
            })
        }, 4000)
    },
    close: function (publisher) {
        clearInterval(publisher.ival)
    },
    sub: function (publisher, obj) {
        publisher.publish(obj)
    },
    requirePersistentLink: true
})



*/