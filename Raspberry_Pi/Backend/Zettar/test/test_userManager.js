var chai = require('chai');
var expect = chai.expect;
var assert = chai.assert;

var dbMan = require('../databaseManager');
var UserManager = require('../userManager');





describe('UserManager', function () {
    describe('database CRUD', function () {
        describe('#addUser', function () {
            it('adds a user to the db', function () {
                return UserManager
                    .addUser({ Username: 'name-test', Surname: 'surname-test', Password:'paas', Age: 100 })
                    .then((user) => {
                        var test = user.getName()
                        expect(user.Username).to.equal('name-test');
                        expect(user.Surname).to.equal('surname-test');
                        expect(user.Age).to.equal(100);
                    })
            })
        })

        describe("#getUser", function () {
            it("gets a user from the db", function () {
                return UserManager
                    .getUser('name-test')
                    .then((user) => {
                        expect(user.Username).to.equal('name-test');
                        expect(user.Surname).to.equal('surname-test');
                        expect(user.Age).to.equal(100);
                    })
            })
        })
    })
})