﻿

const url = require('url');

var express = require('express')
var parseurl = require('parseurl')
var authGuard = require('./lib/authGuard')
var sharedKeys = require('../Shared/sharedKeys')



var app = express()

authGuard.initApp(app)
authGuard.get('/email-confirmation', { web: authGuard.webClass.lowest }, function (req, res, next) {
    var PatientManager = require('./patientManager');
    const querystring = require('querystring')
    PatientManager.validateEmail(querystring.unescape(req.query.a), querystring.unescape(req.query.b)).then(function (got) {
        if (got === 'declined') {
            res.status(200).send('declined')
        } else if (got === 'sucsess'){
            res.status(200).send('sucsessfully registered')
        }
    }).catch(function (e) {
        res.status(400).send(e)
    })
})
authGuard.get('/patient-info', { web: authGuard.webClass.authenticated }, function (req, res, next) {
    var patientManager = require("./patientManager");
    try {
        patientManager.getPatient({ Username: req.revaUser.context.username })
            .then(function (pat) {
                res.status(200).send(JSON.stringify({ zettaletHash: pat.ZettaletUuid }))
            }).catch(function (e) {
                
            })
    } catch (e) {
        res.status(500).send(e)
    }
})





var transport = 'http'
server = module.exports = require(transport).createServer(app);
const WebSocket = require('ws');
const wss = module.exports.wss = new WebSocket.Server({ server });
var wssRoot
wss.whoAmI = function (options) {
    var authorization = ''
    if (options) {
        if (options.user) {
            authorization = options.user + ':' + options.pass + '@'
        }
    }
    return 'ws://' + authorization + wssRoot 
}


var port = 8080
var listenAddress = '197.242.150.255'
if (process.argv.indexOf('--test') >= 0) {
    listenAddress = '127.0.0.1'
    console.log('webServer initialized in testing mode, using link address [' + listenAddress + ']')
}


server.listen(8080, listenAddress, function (err) {
    err && console.log(err)
    wssRoot = server.address().address + (server.address().port !== 80 ? ':' + server.address().port : '')

    server.whoAmI = function (path) {
        return transport + '://' + server.address().address + (server.address().port !== 80 ? ':' + server.address().port : '') + (path ? path : '')
    }
    console.log('\tserver started ', server.address())
})







