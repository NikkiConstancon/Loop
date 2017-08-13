var chai = require('chai');
var expect = chai.expect;
var assert = chai.assert;

var dbMan = require('../databaseManager');
var PatientManager = require('../patientManager');

//ENCRYPTION:
var CryptoJS = require("crypto-js");

var patientKey = "xP{}Lk.x#3V2S?F2p'q{kqd[Qu{7/S-d*bzt"
var accessKey = "4]),`~>{CKjv(E@'d:udH6N@/G4n(}4dn]Mi"
 

/*EXAMPLE USE
// Encrypt 
var ciphertext = CryptoJS.AES.encrypt('my message', 'secret key 123');

// Decrypt 
var bytes  = CryptoJS.AES.decrypt(ciphertext.toString(), 'secret key 123');
var plaintext = bytes.toString(CryptoJS.enc.Utf8);
console.log(plaintext);
*/

/*TABLE STRUCTURE:
module.exports = {
    fields:{
        Username    : "text",           
        PatientPassword    :{  ----> stored as key-value pairs --> as generated by crypto-js
            type: "map",
            typeDef: "<varchar, text>"
        },
        AccessPassword: {    ----> stored as key-value pairs --> as generated by crypto-js
            type: "map",
            typeDef: "<varchar, text>"
        },
        SubscriberList : {      ---> just an ordered array of emails
            type: "set",
            typeDef: "<text>"
        }, 
        PatientEmail     : "text",  
        Address     : "text", 
        Age     : "int",    
        Weight     : "int",
        Height     : "int",
        Reason     : "text" 
    },
    key:["Username"]        
}
*/


describe('PatientManager', function () {
    describe('database CRUD', function () {
        describe('#addPatient', function () {
            it('adds a patient to the db', function () {
                return PatientManager
                    .addPatient({ 
                        Username: 'Username_test',
                        PatientPassword: CryptoJS.AES.encrypt('PatientPassword', patientKey).toString(),
                        AccessPassword: CryptoJS.AES.encrypt('AccessPassword', accessKey).toString(),
                        SubscriberList : ['g@g.com'],
                        PatientEmail : "testPatient@test.co.za", 
                        Address : '42 Dale Avenue Hempton 1765',
                        Age : 42, 
                        Weight : 23,
                        Height : 32, 
                        Reason : 'Disability'})
                    .then((_patient) => {
                        if(_patient != null){
                            expect(_patient.Username).to.equal('Username_test');
                            expect(typeof _patient.PatientPassword).to.equal('string');
                            expect(typeof _patient.AccessPassword).to.equal('string');
                            expect(typeof _patient.SubscriberList).to.equal('object');
                            expect(_patient.PatientEmail).to.equal("testPatient@test.co.za");
                            expect(_patient.Address).to.equal('42 Dale Avenue Hempton 1765');
                            expect(_patient.Age).to.equal(42);
                            expect(_patient.Weight).to.equal(23);
                            expect(_patient.Height).to.equal(32);
                            expect(_patient.Reason).to.equal('Disability');
                        }
                    })
            })
        })

        // describe("#getPatient", function () {
        //     it("gets a user from the db", function () {
        //         return PatientManager
        //             .getPatient('name-test')
        //             .then((user) => {
        //                 expect(user.name).to.equal('name-test');
        //                 expect(user.surname).to.equal('surname-test');
        //                 expect(user.age).to.equal(100);
        //             })
        //     })
        // })
    })
})