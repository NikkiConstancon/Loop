
var dbMan = require('../databaseManager');
var PatientManager = require('../patientManager');
var mailer = require('./mailer')
var logger = require('../revaLog')



var authClassMap = {}
var pathAuthMap = {}
var webClass = {
    lowest: Number.MAX_SAFE_INTEGER,
    authenticated: 500,
    root: 1
}


var authGuard = module.exports = {
    initApp: function (app) {
        authGuard.app = app
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


        authGuard.get('/', { web: webClass.authenticated }, function (req, res, next) {
            res.status(200).send('hi')
            next()
        })
        app.post('/login', function (req, res, next) {
            req.revaUser.login(req, res, next)
        })
        app.post('/registration', function (req, res, next) {
            req.revaUser.signup(req, res, next)
        })
        authGuard.bindAuthResolver('web', function (resolve, reject, req, authRes) {
            var authObj = pathAuthMap[req.path]
            //must explisitly define lower level at path to pass
            if (!authObj) {
                reject()
            } else {
                var level = webClass.lowest
                if (req.revaUser){
                    level = req.revaUser.context.webAuthLevel
                }
                if (authObj.web && authObj.web < level) {
                    authRes.content = { status: 401, send: 'login required' }
                    reject()
                } else {
                    resolve()
                }
            }
        })
        authGuard.bindAuthPath('/login', { web: webClass.lowest })
        authGuard.bindAuthPath('/registration', { web: webClass.lowest })
    },

    /**
    *@breif bind a function to a authClass that must resolve or reject a promise
    *   resolving will allow the url path to execute
    *
    *@example authGuard.bindAuthResolver('web', function (resolve, reject, req, res) {
    *       var authObj = pathAuthMap[req.path]
    *       //must explisitly define lower level at path to pass
    *       if (!authObj) {
    *           res.status(401).send('login required')
    *           reject()
    *       } else {
    *           var level = webClass.lowest
    *           if (req.revaUser){
    *               level = req.revaUser.context.webAuthLevel
    *           }
    *           if (authObj.web && authObj.web < level) {
    *               res.status(401).send('login required')
    *               reject()
    *           } else {
    *               resolve()
    *           }
    *       }
    *   })
    **/
    //@param fun, will be called as promise, so must have resolve and reject
    bindAuthResolver: function (authClass, fun) {
        authClassMap[authClass] = fun
    },
    /**
    *@param auth {class:level, class:level,...} smaller valus have heigher auth level
    *NB do not use 0, as this might skew logic due to zero casting to false
    **/
    bindAuthPath: function (path, auth) {
        pathAuthMap[path] || (pathAuthMap[path] = {})
        for (var key in auth) {
            pathAuthMap[path][key] = auth[key]
        }
    },
    get: function (path, auth, fun) {
        authGuard.bindAuthPath(path, auth)
        authGuard.app.get(path,fun)
    },
    post: function (path, auth, fun) {
        authGuard.bindAuthPath(path, auth)
        authGuard.post.get(path, fun)
    },
    webClass: webClass
}



function bootstrapSession(req, res, next) {
    if (!req.session.userContext) {
        req.session.userContext = UserSession.createContext()
    }
    req.revaUser = new UserSession(req)
    req.revaUser.context.lastUrl = req.path

    /*var arr = [];
    for (var c in authClassMap) {
        arr.push(c)
    }
    const loop = function (i) {
        if (i >= arr.length) { return }
        if (pathAuthMap[req.path]) {
            return new Promise(function (resolve, reject) {
                return authClassMap[c](resolve, reject, req, res)
            }).then(function () {
                next()
            }).catch(function () { loop(i + 1)})
        }
    }
    loop(0)*/
    var arr = [];
    for (var c in authClassMap) {
        arr.push(c)
    }

    function authResponse() {
        var storeKey = Symbol()
        this[storeKey] = { contentSet: false, content: { status: 401, send: 'access denied' }}
        Object.defineProperty(this, 'content', {
            get: function () { return this[storeKey].content },
            set: function (value) {
                if (!this[storeKey].contentSet) {
                    this[storeKey].contentSet = true
                    this[storeKey].content = value
                }
            }
        })
    }

    var obj = new authResponse()
    const loop = function (i) {
        if (i >= arr.length) {
            res.status(obj.content.status).send(obj.content.send)
            res.end()
        } else if (pathAuthMap[req.path]) {
            return new Promise(function (resolve, reject) {
                return authClassMap[c](resolve, reject, req, obj)
            }).then(function () {
                next()
            }).catch(function () { loop(i + 1) })
        } else {
            res.status(obj.content.status).send(obj.content.send)
            res.end()
        }
    }
    loop(0)
}

function UserSession(req) {
    this.context = req.session.userContext
    this.context.requestCount++;
}
UserSession.createContext = function () {
    return { webAuthLevel: webClass.lowest, requestCount: 0 };
}
UserSession.prototype.signup = function (req, res, next) {

    //TODO: Move to patiantManager
    function deserialize(body) {
        var test = require('../models/patientModel').fields
       // body.Password = CryptoJS.AES.encrypt(body.Password, patientKey).toString()
       // body.AccessPassword = CryptoJS.AES.encrypt(uuidv1(), accessKey).toString()
        for (var key in test) {
            switch (test[key].type) {
                case 'int': { body[key] = parseInt(body[key]) } break
                case 'float': { body[key] = parseFloat(body[key]) } break
            }
        }
    }
    deserialize(req.body)

    PatientManager.addPatient(req.body).then(function (pat) {
        res.status(201).send('user created')
    }).catch(function (e) {
        res.status(422).send(e)
    })
}
UserSession.prototype.login = function (req, res, next) {
    var self = this;
    PatientManager.getPatient({ Username: req.body.Username }).then(function (pat) {
        if (pat.verifyPassword(req.body.Password)) {
            self.context.webAuthLevel = webClass.authenticated;
            res.status(200).send('login successful');
            next()
        } else {
            res.status(401).send('incorrect user or pass');
            res.end()
        }
    }).catch(function (e) {
        res.status(401).send(e);
    })
}











