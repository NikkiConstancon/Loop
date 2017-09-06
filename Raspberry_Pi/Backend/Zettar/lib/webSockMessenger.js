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








var requirePersistentLinkMap = {}//set to true if publishers' messages should be delivered even if the app is not open
var connecttors = {}
var closeors = {}
var subscribers = {}
var requiered = ['connect', 'close', 'sub']
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
        if (connecttors[key]) {
            throw new Error('@webSockMessenger: key ' + key + ' hase already been attach')
        }
        for (var i in requiered) {
            if (!options[requiered[i]] || !(options[requiered[i]] instanceof Function)) {
                throw new Error('@webSockMessenger#attach: option ' + requiered[i] + ' of key ' + key + ' is missing or not a function')
            }
        }
        connecttors[key] = options.connect//functon(user, publishFun(msg, errcb)
        subscribers[key] = options.sub //funcction(user, message)
        closeors[key] = options.close//functon(user)
        if (options.requirePersistentLink) {
            requirePersistentLinkMap[key] = true
        }
    },
    getUserSocketContextMap: function () { return userSocketContextMap },
    getUserSocketContext: function (user) { return userSocketContextMap[user] }
}
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

/**
 * 
 * @param {any} field is a filed
 */
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

/**
 * 
 * @param {any} ws is the websocket
 */
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
    var key = serviceKey
    var user = userId
    this.serviceKey = serviceKey
    this.userId = userId;

    var queue = []
    var timeOut = null
    function pushMessage(msg, errcb) {
        var errcb = errcb
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
                        delete userSocketContextMap[user][connectUid]
                    } catch (e) {
                        logger.error('@WebSockMessenger#connection:delete', e)
                    }
                    ws.close()
                }
            })
        }
    }
    this.publisher = function (msg, errcb) {
        if (!ws.ipc_sevice_bound && !requirePersistentLinkMap[key]) {
            return
        }
        queue.push(msg)
        if (!timeOut) {
            timeOut = setTimeout(function () {
                pushMessage(queue, errcb)
                queue = []
                timeOut = null
            },8)//for accumalting data befor sending
        }
    }
    this.publisher.ws = ws
    this.publisher.user = user
    this.publisher.context = this
    connecttors[key](this.publisher)
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
    ws.ipc_sevice_bound = false//deactivate moste servecies to stop thrashing the connection if app not in focus. NOTE! ws is shared
    ws.send(getGreeting(user), function (err) {
        if (err) { logger.error('@WebSockMessenger', err) }
    })
    
    for (var key in connecttors) {
        publisherMap[key] = new ServicePublisher(this, key, user, ws, connectUid)
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

/**
 *@brief attach all the procedure clusters at client connection
 **/

wss.on('connection', function connection(ws) {
    authorizeAsync(ws).then(function (param) {
        new userSocketContext(param)
    }).catch(function (e) {
        ws.send(e)
        ws.close()
        e && logger.error(e)
    })

})




webSockMessenger.attach('UserManager', {
    connect: function (publisher) {
        publisher("--HELLO-- " + publisher.user);
    },
    close: function (publisher) {
    },
    sub: function (publisher, obj) {
        console.log(obj);
        if (obj === "IPC_SEVICE_BOUND_COUNT=0") {
            publisher.ws.ipc_sevice_bound = false
            return
        } else if (obj === "IPC_SEVICE_BOUND_COUNT!=0") {
            publisher.ws.ipc_sevice_bound = true
            return
        }
        if (obj.TEST_EMAIL_AVAILABLE) {
            obj.TEST_EMAIL_AVAILABLE = true;
            publisher(obj);
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
                    publisher({ KEY_REGISTER_USER: { Username: 'is already taken' } })
                }).catch(function () {
                    return PatientManager.addPatient(obj).then(function (pat) {
                        publisher({ KEY_REGISTER_USER: true })
                    }).catch(function (e) {
                        publisher({ KEY_REGISTER_USER: { ERROR: e.message || e }})
                    })
                }).catch(function (e) {
                    logger.error("@webSockMessenger$UserManager#sub:KEY_REGISTER_USER", e)
                    publisher({ KEY_REGISTER_USER: { ERROR: e.message || e }})
                })
        }
        logger.info(obj);
    },
    requirePersistentLink:true
})




webSockMessenger.attach('Pulse', {
    connect: function (publisher) {
        var count = 0;
        publisher.ival = setInterval(function () {
            publisher(count++, function (err) {
                err && clearInterval(publisher.ival)
            })
        }, 4000)
    },
    close: function (publisher) {
        clearInterval(publisher.ival)
    },
    sub: function (publisher, obj) {
        publisher(obj)
    },
    requirePersistentLink: true
})



