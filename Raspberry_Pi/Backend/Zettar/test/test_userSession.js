var chai = require('chai');
var request = require('request').defaults({ jar: true })
var uuidv1 = require('uuid/v1')
var expect = chai.expect
var assert = chai.assert



var logger = require('../revaLog');







describe('UserSession', function () {
    describe('server response', function () {
        before(function () {
            server = require('../lib/revaServer.js')
            lastLogLevel = logger.level
            logger.level = -1;
        });
        it('with unauthenticated connection', function (done) {
            request.post({
                url: 'http://localhost:3000',
                form: { key: 'value' }
            }, function (err, res, body) {
                expect(res.statusCode).to.equal(401)
                done()
            })
        });
        describe('test user registration', function () {
            it('with invalid username', function (done) {
                request.post({
                    url: 'http://localhost:3000/registration',
                    form: {
                    }
                }, function (err, res, body) {
                    expect(res.statusCode).to.equal(412)
                    done()
                })
            })
            it('with valid arguments', function (done) {
                request.post({
                    url: 'http://localhost:3000/registration',
                    form: {
                        Username: 'Username_test',
                        PatientPassword: 'PatientPassword',
                        SubscriberList: ['g@g.com'],
                        PatientEmail: "testPatient@test.co.za",
                        Address: '42 Dale Avenue Hempton 1765',
                        Age: 42,
                        Weight: 23,
                        Height: 32,
                        Reason: 'Disability'
                    }
                }, function (err, res, body) {
                    expect(res.statusCode).to.equal(201)
                    done()
                })
            })
        })
        describe('test user login', function () {
            it('with invalid password', function (done) {
                request.post({
                    url: 'http://localhost:3000/login',
                    form: {
                        Username: 'Username_test',
                        PatientPassword: uuidv1(),
                    }
                }, function (err, res, body) {
                    expect(res.statusCode).to.equal(401)
                    done()
                })
            })
            it('with invalid username', function (done) {
                request.post({
                    url: 'http://localhost:3000/login',
                    form: {
                        Username: uuidv1(),
                        PatientPassword: uuidv1(),
                    }
                }, function (err, res, body) {
                    expect(res.statusCode).to.equal(401)
                    done()
                })
            })
            it('with valid arguments', function (done) {
                request.post({
                    url: 'http://localhost:3000/login',
                    form: {
                        Username: 'Username_test',
                        PatientPassword: 'PatientPassword',
                    }
                }, function (err, res, body) {
                    expect(res.statusCode).to.equal(200)
                    done()
                })
            })
            it('with authenticated connection', function (done) {
                request.get('http://localhost:3000', function (err, res, body) {
                    expect(res.statusCode).to.equal(200)
                    done()
                })
            });
        })
        after(function () {
            logger.level = lastLogLevel
            server.close();
        });
    })
});

