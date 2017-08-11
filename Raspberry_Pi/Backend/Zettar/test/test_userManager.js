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
                    .addUser({ name: 'name-test', surname: 'surname-test', age: 100 })
                    .then((user) => {
                        expect(user.name).to.equal('name-test');
                        expect(user.surname).to.equal('surname-test');
                        expect(user.age).to.equal(100);
                    })
            })
        })

        describe("#getUser", function () {
            it("gets a user from the db", function () {
                return UserManager
                    .getUser('name-test')
                    .then((user) => {
                        expect(user.name).to.equal('name-test');
                        expect(user.surname).to.equal('surname-test');
                        expect(user.age).to.equal(100);
                    })
            })
        })
    })
})