var express = require('express')
var parseurl = require('parseurl')
var authGuard = require('./lib/authGuard')


var app = express()
authGuard.initApp(app)
authGuard.get('/email-confirmation', { web: authGuard.webClass.lowest }, function (req, res, next) {
    var PatientManager = require('./patientManager');
    const querystring = require('querystring')
    PatientManager.validateEmail(querystring.unescape(req.query.a), querystring.unescape(req.query.b)).then(function () {
        res.status(200).send('hi there')
    }).catch(function (e) {
        res.status(400).send(e)
    })
})




var transport = 'http'
server = module.exports = require(transport).createServer(app);
server.listen(80,'127.0.0.1', function (err) {
    err && console.log(err)


    server.whoAmI = function (path) {
        return transport + '://' + server.address().address + (server.address().port !== 80 ? ':' + server.address().port : '') + (path ? path : '')
    }
    console.log('\tserver started ', server.address())
})

