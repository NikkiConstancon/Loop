/**
 * @file
 * Test the webserver's authentication system
 *
 * @notice logging  is turned off to avid collision with mocha test output
 **/

var chai = require('chai');
var request = require('request').defaults({ jar: true })
var uuidv1 = require('uuid/v1')
var expect = chai.expect
var assert = chai.assert



var logger = require('../revaLog');
var server = require('../webServer.js')





var lastLogLevel;
var username = 'authGuard ' + uuidv1()
xdescribe('AuthGuard', function () {
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
            it('with unset username in headder', function (done) {
                request.post({
                    url: server.whoAmI('/registration'),
                    form: {                    }
                }, function (err, res, body) {
                    expect(res.statusCode).to.equal(422)
                    expect(res.body).to.equal('{"error_head":"Username"}')
                    done()
                })
            })
            it('with unset password in headder', function (done) {
                request.post({
                    url: server.whoAmI('/registration'),
                    form: { Username:'hello' }
                }, function (err, res, body) {
                    expect(res.statusCode).to.equal(422)
                    expect(res.body).to.equal('{"error_head":"Password"}')
                    done()
                })
            })
            it('with invalid usernamer', function (done) {
                request.post({
                    url: server.whoAmI('/registration'),
                    form: {
                        Username: 'ab',
                        Password: 'set'
                    }
                }, function (err, res, body) {
                    expect(res.statusCode).to.equal(422)
                    expect(res.body).to.equal('{"Username":"must be longer than two characters"}')
                    done()
                })
            })
            it('with invalid profane name', function (done) {
                request.post({
                    url: server.whoAmI('/registration'),
                    form: {
                        Username: 'shit face',
                        Password: 'set'
                    }
                }, function (err, res, body) {
                    expect(res.statusCode).to.equal(422)
                    expect(res.body).to.equal('{"Username":"profane names are not allowed"}')
                    done()
                })
            })
            form = {
                Username: username,
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
                        Username: username,
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
                        Username: 'reva user',
                        Password: 'password',
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
            })
            it('at path /patient-info', function (done) {
                request.get(server.whoAmI('/patient-info'), function (err, res, body) {
                    //expect(res.statusCode).to.equal(200)
                    //expect(JSON.parse(res.body)).to.deep.include({ zettaletHash: 'U2FsdGVkX19VHQRYZNi4vKkc8FC9JuYR7hh25NwCAek=U2FsdGVkX19VHQRYZNi4vKkc8FC9JuYR7hh25NwCAek=' })
                    done()
                })
            })
        })
        after(function () {
            logger.level = lastLogLevel
        });
    })
});

