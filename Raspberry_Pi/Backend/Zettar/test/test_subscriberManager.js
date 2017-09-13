/**
 * @file
 * Test the database singleton subscriber manager
 *
 * @notice logging  is turned off to avid collision with mocha test output
 **/

var chai = require('chai');
var expect = chai.expect;
var assert = chai.assert;

var uuidv1 = require('uuid/v1')

var dbMan = require('../databaseManager');
var subscriberManager = require('../subscriberManager');

var mailer = require('../lib/mailer')

//ENCRYPTION:
var CryptoJS = require("crypto-js");

var subscriberKey = "xP{}Lk.x#3V2S?F2p'q{kqd[Qu{7/S-d*bzt"
var accessKey = "4]),`~>{CKjv(E@'d:udH6N@/G4n(}4dn]Mi"


var userEmail = "testing@gmail.com"

describe('subscriberManager', function () {
    describe('database CRUD', function () {
        describe('#addsubscriber', function () {
            it('adds a subscriber to the db', function () {
                return subscriberManager
                    .addSubscriber({ 
                        Email: userEmail,
                        Password: CryptoJS.AES.encrypt('Password', 'secret key 123').toString(),
                        Relation: "doctor",
                        PatientList: []
                    })
                    .then((_subscriber) => {
                        if(_subscriber != null){
                            expect(_subscriber.Email).to.equal(userEmail);
                            expect(typeof _subscriber.Password).to.equal('string');
                            expect(typeof _subscriber.PatientList).to.equal('object');
                            expect(_subscriber.Relation).to.equal('doctor');
                        }
                    })
            })
        })

       describe("#checkSubscriberExists", function () {
            it("username is already in use", function () {
                return subscriberManager
                    .checkSubscriberExists(userEmail)
                    .then((user) => {
                        expect(user).to.equal(true);
                    })
            })
            it("username is not in use", function () {
                return subscriberManager
                    .checkSubscriberExists("no-One")
                    .then((user) => {
                        expect(user).to.equal(false);
                    })
            })
        })

        describe("#getsubscriber", function () {
            it("gets a subscriber from the db", function () {
                return subscriberManager
                    .getsubscriber({Email : userEmail})
                    .then((user) => {
                        expect(user.Email).to.equal(userEmail);
                    })
            })
        })
    })
    
})