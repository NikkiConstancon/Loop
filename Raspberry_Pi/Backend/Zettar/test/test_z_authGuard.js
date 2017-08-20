var chai = require('chai');
var request = require('request').defaults({ jar: true })
var uuidv1 = require('uuid/v1')
var expect = chai.expect
var assert = chai.assert



var logger = require('../revaLog');
var server = require('../webServer.js')





var lastLogLevel;

describe('AuthGuard', function () {
    describe('server response', function () {
        before(function () {
            lastLogLevel = logger.level
            logger.level = -1 //'silly';
        });
        it('with unauthenticated connection', function (done) {
            request.post({
                url: server.whoAmI(),
                form: { key: 'value' }
            }, function (err, res, body) {
                expect(res.statusCode).to.equal(401)
                done()
            })
        });
        describe('test user registration', function () {
            it('with invalid username', function (done) {
                request.post({
                    url: server.whoAmI('/registration'),
                    form: {
                    }
                }, function (err, res, body) {
                    expect(res.statusCode).to.equal(422)
                    done()
                })
            })
            form = {
                Username: 'authGuard ' + uuidv1(),
                Password: 'Password',
                SubscriberList: ['g@g.com'],
                Email: 'COS332.Marthinus@gmail.com',
                Address: '42 Dale Avenue Hempton 1765',
                Age: 42,
                Weight: 23,
                Height: 32,
                Reason: 'Disability'
            }
            it('with valid arguments', function (done) {
                request.post({
                    url: server.whoAmI('/registration'),
                    form: form
                }, function (err, res, body) {
                    expect(res.statusCode).to.equal(201)
                    done()
                })
            })
            it('with existing user arguments', function (done) {
                request.post({
                    url: server.whoAmI('/registration'),
                    form: form
                }, function (err, res, body) {
                    expect(res.statusCode).to.equal(422)
                    done()
                })
            })
        })
        describe('test user login', function () {
            it('with invalid password', function (done) {
                request.post({
                    url: server.whoAmI('/login'),
                    form: {
                        Username: 'Server Session Test',
                        Password: uuidv1(),
                    }
                }, function (err, res, body) {
                    expect(res.statusCode).to.equal(401)
                    done()
                })
            })
            it('with invalid username', function (done) {
                request.post({
                    url: server.whoAmI('/login'),
                    form: {
                        Username: uuidv1(),
                        Password: uuidv1(),
                    }
                }, function (err, res, body) {
                    expect(res.statusCode).to.equal(401)
                    done()
                })
            })
            it('with valid arguments', function (done) {
                request.post({
                    url: server.whoAmI('/login'),
                    form: {
                        Username: 'Server Session Test',
                        Password: 'Password',
                    }
                }, function (err, res, body) {
                    expect(res.statusCode).to.equal(200)
                    done()
                })
            })
            it('with authenticated connection', function (done) {
                request.get(server.whoAmI(), function (err, res, body) {
                    expect(res.statusCode).to.equal(200)
                    done()
                })
            });
        })
        after(function () {
            logger.level = lastLogLevel
        });
    })
});

