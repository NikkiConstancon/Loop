﻿
var dbMan = require('../databaseManager');
var PatientManager = require('../patientManager');
var mailer = require('./mailer')
var logger = require('../revaLog')


var CryptoJS = require("crypto-js");
var patientKey = "xP{}Lk.x#3V2S?F2p'q{kqd[Qu{7/S-d*bzt"
var accessKey = "4]),`~>{CKjv(E@'d:udH6N@/G4n(}4dn]Mi"

var uuidv1 = require('uuid/v1')

const HOSTNAME = require('os').hostname()




module.exports = function (app) {
    var session = require('express-session')
    var bodyParser = require('body-parser')
    var uuidv1 = require('uuid/v1')

    app.use(session({
        secret: uuidv1(),
        resave: false,
        saveUninitialized: true
    }))
    app.use(bodyParser.json());       // to support JSON-encoded bodies
    app.use(bodyParser.urlencoded({     // to support URL-encoded bodies
        extended: true
    }));

    app.use(bootstrapSession)


    app.get('/emial-confirmation', function(req, res, next) {
        req.revaUser.emialConfirmation(req, res, next)
    })

    app.get('/', function (req, res, next) {
        res.status(200).send('hi')
        next()
    })
    app.post('/login', function (req, res, next) {
        req.revaUser.login(req, res, next)
    })
    app.post('/registration', function (req, res, next) {
        req.revaUser.signup(req, res, next)
    })
}



function bootstrapSession(req, res, next) {
    if (!req.session.userContext) {
        req.session.userContext = UserSession.createContext()
    }
    req.revaUser = new UserSession(req)

    req.revaUser.isLoggedOn(req, res, next)
    req.revaUser.validateAuthorizatiron(req, res, next)
    next()
}

function UserSession(req) {
    this.context = req.session.userContext
    this.context.requestCount++;
}
UserSession.createContext = function () {
    return { loggedOn: false, requestCount: 0 };
}
UserSession.prototype.isLoggedOn = function (req, res, next) {
  //TODO: fix this &&&&& with low auth path list
    if (!this.context.loggedOn && req.path !== '/login' && req.path !== '/registration' && req.path !== '/emial-confirmation') {
        this.context.lastUrl = req.path
        res.status(401).send('login required')
        //res.redirect("/login");
    }
}
UserSession.prototype.validateAuthorizatiron = function (req, res, next) {
}

UserSession.prototype.signup = function (req, res, next) {

    //TODO: Move to patiantManager
    function deserialize(body) {
        var test = require('../models/patientModel').fields
        body.PatientPassword = CryptoJS.AES.encrypt(body.PatientPassword, patientKey).toString()
        body.AccessPassword = CryptoJS.AES.encrypt(uuidv1(), accessKey).toString()
        for (var key in test) {
            switch (test[key].type) {
                case 'int': { body[key] = parseInt(body[key]) } break
                case 'float': { body[key] = parseFloat(body[key])}break
            }
        }
    }
    deserialize(req.body)

    PatientManager.addPatient(req.body).then(function (pat) {
        res.status(201).send('user created & email not sent')
        mailer.mailEmialConfirmationUrl(pat, req.headers.host).catch(function (e) {
        })
    }).catch(function (e) {
        res.status(412).send(e)
    })
}
UserSession.prototype.login = function (req, res, next) {
    var self = this;
    PatientManager.getPatient({ Username: req.body.Username }).then(function (pat) {
        var truePass = CryptoJS.AES.decrypt(pat.PatientPassword.toString(), patientKey).toString(CryptoJS.enc.Utf8)



        if (truePass !== req.body.PatientPassword) {
            res.status(401).send('incorrect user or pass');
//NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB
            res.end()
//NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB--NB
        } else {
            self.context.loggedOn = true;
            res.status(200).send('login successful');
            next()
        }
    }).catch(function (e) {
        res.status(401).send(e);
    })
}

UserSession.prototype.emialConfirmation = function (req, res, next) {
    res.status(200).send('nice')
}









