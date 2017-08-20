﻿const logger = require('../revaLog')
const server = require('../webServer')



const WebSocket = require('ws');
const wss = module.exports.wss = server.wss


const patientManager = require('../patientManager')








var connecttors = {}
var closeors = {}
var subscribers = {}
var requiered = ['connect', 'close', 'sub']
const webSockMessenger = module.exports = {
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
    }
}


function parseError(key, error, message) {
    var obj = {  }
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


function parseAuthorizationHeader(field) {
    return new Promise(function (res, rej) {
        if (!field) {
            rej(parseError('webSockMessenger', 'authorization header not set, though it is required'))
        }
        try {
            var auth = field.split(' ')
            var str = new Buffer(auth[1], 'base64').toString()
            var strs = str.split(':')
            res({ Username: strs[0], Password: strs[1] })
        } catch (e) {
            rej(parseError(parseError('webSockMessenger', e)))
        }
    })
}


function authorizeAsync(ws) {
    return new Promise(function (res, rej) {
        return parseAuthorizationHeader(ws.upgradeReq.headers.authorization).then(function (auth) {
            patientManager.getPatient(auth).then(function (pat) {
                if (pat.verifyPassword(auth.Password)) {
                    res({ ws: ws, user: pat })
                } else {
                    rej(parseError('webSockMessenger', 'invalid password'))
                }
            }).catch(function (e) {
                rej(parseError('webSockMessenger', e))
            })
        }).catch(function (err) {
            rej(err)
        })
    })
}




wss.on('connection', function connection(ws) {
    authorizeAsync(ws).then(function (param) {
        var ws = param.ws
        var user = param.user

        ws.send(getGreeting(user), function (err) {
            logger.error('@WebSockMessenger', err)
        })

        for (var key in connecttors) {
            connecttors[key](user, function (msg, errcb) {
                var errcb = errcb
                msgObj = { [key]: msg }
                msg = JSON.stringify(msgObj)
                if (errcb) {
                    ws.send(msg, errcb)
                } else {
                    ws.send(msg, function (err) {
                        logger.error('@WebSockMessenger#connection', err)
                    })
                }
            })
        }
        ws.on('message', function (message) {
            try {
                var json = JSON.parse(message)
                for (var k in json) {
                    subscribers[k] && subscribers[k](user, json[k])
                }
            } catch (e) {
                var errMsg = JSON.stringify({
                    error: { msg: message, err: e.message }
                })
                logger.error(errMsg)
                ws.send(errMsg)
            }
        });


        /*
        var ival = setInterval(function () {
            ws.send('pushing to you ^::^', function (err) {
                err && clearInterval(ival)
            });
        }, 1000)*/


        ws.on('close', function close() {
            for (var k in closeors) {
                closeors[k](user)
            }
        });
    }).catch(function (e) {
        ws.send(e)
        ws.close()
        e && logger.error(e)
    })

})









/*

webSockMessenger.attach('Alert', {
    connect: function (user, send) {
        var ival = setInterval(function () {
            send('pushing to you', function (err) {
                err && clearInterval(ival)
            });
        }, 1000)
    },
    close: function (user) {
    },
    sub: function (user, obj) {
    }
})

*/