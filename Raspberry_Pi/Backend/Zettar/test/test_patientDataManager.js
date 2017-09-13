/**
 * @file
 * Test the database singleton patient data manager
 *
 * @notice logging  is turned off to avid collision with mocha test output
 **/

var chai = require('chai');
var expect = chai.expect;
var assert = chai.assert;

var uuidv1 = require('uuid/v1')

var dbMan = require('../databaseManager');
var PatientDataManager = require('../patientDataManager');


var userName = 'Patient'

describe('PatientDataManager', function () {
  /*  describe('database CRUD', function () {
        describe('#addInstance', function () {
            it('adds a patient to the db', function () {
                return PatientManager
                    .addInstance({ 
                        Username: userName,
                        Password: CryptoJS.AES.encrypt('Password', patientKey).toString(),
                        AccessPassword: CryptoJS.AES.encrypt('AccessPassword', accessKey).toString(),
                        SubscriberList : ['g@g.com'],
                        Email : "testPatient@test.co.za", 
                        Address : '42 Dale Avenue Hempton 1765',
                        Age : 42, 
                        Weight : 23,
                        Height : 32, 
                        Reason : 'Disability'})
                    .then((_patient) => {
                        if(_patient != null){
                            // expect(_patient.Username).to.equal(userName);
                            // expect(typeof _patient.Password).to.equal('string');
                            // expect(typeof _patient.AccessPassword).to.equal('string');
                            // expect(typeof _patient.SubscriberList).to.equal('object');
                            // expect(_patient.Email).to.equal("testPatient@test.co.za");
                            // expect(_patient.Address).to.equal('42 Dale Avenue Hempton 1765');
                            // expect(_patient.Age).to.equal(42);
                            // expect(_patient.Weight).to.equal(23);
                            // expect(_patient.Height).to.equal(32);
                            // expect(_patient.Reason).to.equal('Disability');
                        }
                    })
            })
        })

        describe("#getPatient", function () {
            it("gets a user from the db", function () {
                return PatientManager
                    .getPatient({Username : userName})
                    .then((user) => {
                        expect(user.Username).to.equal(userName);
                    })
            })
        })
    })*/

})