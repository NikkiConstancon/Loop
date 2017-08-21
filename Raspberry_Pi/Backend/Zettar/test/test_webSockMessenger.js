var chai = require('chai');
var request = require('request').defaults({ jar: true })
var uuidv1 = require('uuid/v1')
var expect = chai.expect
var assert = chai.assert




var logger = require('../revaLog');
var server = require('../webServer')

var webSockMessenger = require('../lib/webSockMessenger')




var lastLogLevel;
const WebSocket = require('ws')
var ws
var query
describe('@zettaletEventsDispatcher', function (done) {
    before(function (done) {
        lastLogLevel = logger.level
        logger.level = -1
        query = require('../demo/createDummyPatient').query
        done()
    })
    describe('testing server response at socket connection', function () {
        describe('based on http authorization header', function () {
            it('when not specified', function (done) {
                ws = new WebSocket(server.wss.whoAmI())
                ws.on('open', function open() {
                })
                ws.on('message', function incoming(data) {
                    obj = JSON.parse(data)
                    expect(obj).to.deep.include({ "error": { "webSockMessenger": { "server": "authorization header not set, though it is required" } } })
                    done()
                })
            })
            it('with non matching user', function (done) {
                ws = new WebSocket(server.wss.whoAmI({ user: 'nope-nope', pass: query.Password }))
                ws.on('open', function open() {
                })
                ws.on('message', function incoming(data) {
                    obj = JSON.parse(data)
                    expect(obj).to.deep.include({ "error": { "webSockMessenger": { "server": "invalid username" } } })
                    done()
                })
            })
            it('with invalid password', function (done) {
                ws = new WebSocket(server.wss.whoAmI({ user: query.Username, pass: "asdfadsfadsf" }))
                ws.on('open', function open() {
                })
                ws.on('message', function incoming(data) {
                    obj = JSON.parse(data)
                    expect(obj).to.deep.include({ "error": { "webSockMessenger": { "server": "invalid password" } } })
                    done()
                })
            })
            it('with matching user credentials', function (done) {
                ws = new WebSocket(server.wss.whoAmI({ user: query.Username, pass: query.Password }))
                ws.on('open', function open() {
                })
                ws.on('message', function incoming(data) {
                    obj = JSON.parse(data)
                    expect(obj).to.deep.include({ "init": { "webSockMessenger": "succsess" } })
                    done()
                })
            })
        })
    })


    describe('testing attachments', function () {
        describe('when binding with #attach', function () {
            it('and missing @param key', function (done) {
                assert.throws(function() {
                    webSockMessenger.attach({
                        close: function (user) { },
                        sub: function (user, obj) { }
                    })
                }, Error, '@webSockMessenger#attach: key or options are not defied')
                done()
            })
            it('and missing @param key', function (done) {
                assert.throws(function () {
                    webSockMessenger.attach('TEST', {
                        close: function (user) { },
                        sub: function (user, obj) { }
                    })
                }, Error, '@webSockMessenger#attach: option connect of key TEST is missing or not a function')
                done()
            })
            it('and invalid @param close', function (done) {
                expect(function () {
                    webSockMessenger.attach('TEST', {
                        connect: function (user, send) { },
                        sub: function (user, obj) { }
                    })
                }).throw(Error, '@webSockMessenger#attach: option close of key TEST is missing or not a function')
                done()
            })

            it('and invalid @param sub', function (done) {
                expect(function () {
                    webSockMessenger.attach('TEST', {
                        connect: function (user, send) { },
                        close: function (user) { },
                    })
                }).throw(Error, '@webSockMessenger#attach: option sub of key TEST is missing or not a function')
                done()
            })
            it('and valid parameters', function (done) {
                expect(function () {
                    webSockMessenger.attach('TEST-DUP', {
                        connect: function (user, send) { },
                        close: function (user) { },
                        sub: function (user, obj) { }
                    })
                }).to.not.throw()
                done()
            })
            it('and duplicat key', function (done) {
                expect(function () {
                    webSockMessenger.attach('TEST-DUP', {
                        connect: function (user, send) { },
                        close: function (user) { },
                        sub: function (user, obj) { }
                    })
                }).to.throw(Error, '@webSockMessenger: key TEST-DUP hase already been attach')
                done()
            })
        })
        describe('testing publish and subscribeing', function () {
            this.timeout(8000)
            var doneToCall = true;
            before(function () {
                ws = new WebSocket(server.wss.whoAmI({ user: query.Username, pass: query.Password }))
                ws.on('open', function () {
                    var numSend = 0
                    var ival = setInterval(function () {
                        ws.send(JSON.stringify({ TEST: "pub" }))
                        if (numSend++ == 5) {
                            clearInterval(ival)
                            ws.close()
                        }
                    }, 100)
                })
            })
            var foundOne = false
            it('test #connect, #close, and #sub', function (done) {
                var subTrue = false
                webSockMessenger.attach('TEST', {
                    connect: function (user, send) {
                        var ival = setInterval(function () {
                            send('sub', function (err) {
                                err && clearInterval(ival)
                            });
                        }, 1000)
                    },
                    close: function (user) {
                        if (foundOne){
                            done()
                        } else {
                            assert(false,'did not find the sub message')
                        }
                    },
                    sub: function (user, obj) {
                        foundOne = true
                    }
                })
            })
            xit('message arrival', function (done) {
                ws.on('open', function open() {
                })
                ws.on('message', function incoming(data) {
                    obj = JSON.parse(data)
                    expect(obj).to.deep.include({ "TEST": "sub" })
                    done()
                })
            })
        })
    })


    after(function (){
        logger.level = lastLogLevel
    })
})