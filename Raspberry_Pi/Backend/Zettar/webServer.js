/**
 * @file
 * This file boots up the Webserver that is responsible of receiving requests from the android client
 * and then preforming the intended procurers and serving the appropriate response.
 *
 * @arg --test is a stdin argument that will activate the testing suit intertwined within  most modules
 * @arg --test-keepAlive is a stdin argument that will prevent the server from killing itestlf after execution
 * @arg --test-drop is a stdin argument that will cause the database to be automatically dropped after execution
 **/

const url = require('url');

var express = require('express')
var parseurl = require('parseurl')
var authGuard = require('./lib/authGuard')
var sharedKeys = require('../Shared/sharedKeys')

var fs = require('fs');
var privateKey = fs.readFileSync('../Shared/reva-key.pem', 'utf8');
var certificate = fs.readFileSync('../Shared/reva-cert.pem', 'utf8');
var credentials = { key: privateKey, cert: certificate };



var app = express()


/**
 * @brief
 * connect the procudure to the server that will handle client email validation
 *
 * @precondition [none] This resource is available to all
 */
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


/**
 * @brief
 * connect the procudure to the server that will handle patient information. The information served
 * will include the Zettarlet URI and the avialible devices.
 *
 * @precondition This resour is guarded and requiers normal system authentication to be accessed
 */
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

//initalization of the server
var transport = 'http'
server = module.exports = httpsServer = require('https').createServer(credentials, app)//require(transport).createServer(app);
module.exports.whoAmI = function (param) { return serverAmWhoAmI(param) }
const WebSocket = require('ws');
const wss = module.exports.wss = new WebSocket.Server({ server });
var wssRoot
/**
 * @return the the websocket's address that is currently listening on the machine
 */
wss.whoAmI = function (options) {
    var authorization = ''
    if (options) {
        if (options.user) {
            authorization = options.user + ':' + options.pass + '@'
        }
    }
    return 'ws://' + authorization + wssRoot 
}


/**
 * @brief get stdin paramters for testing to listen on local host
 */
var port = 8080
var listenAddress = '197.242.150.255'
if (process.argv.indexOf('--test') >= 0) {
    listenAddress = '127.0.0.1'
    console.log('webServer initialized in testing mode, using link address [' + listenAddress + ']')
}

/**
 * @brief start the web server
 */
server.listen(8080, function (err) {
    err && console.log(err)
    wssRoot = server.address().address + (server.address().port !== 80 ? ':' + server.address().port : '')

    serverAmWhoAmI = function (path) {
        return transport + '://' + server.address().address + (server.address().port !== 80 ? ':' + server.address().port : '') + (path ? path : '')
    }
    console.log('\tserver started ', server.address())
})
var serverAmWhoAmI = function () { return "" }


/*

var httpsServer = require('https').createServer(credentials, app);
httpsServer.listen(8443);

var wsss = new WebSocket.Server({
    server: httpsServer
});

wsss.on('connection', function connection(ws) {
    ws.on('message', function incoming(message) {
        console.log('received: %s', message);
    });

    ws.send('something');
});



*/


