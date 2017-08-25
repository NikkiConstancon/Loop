/**
 * @file
 * Cassandra database model that describes the User schema
 * It also defines some methods to pe performed on Users
 **/

var Model = require('../lib/modelClass')

const badWords = require('bad-words')
const filter = new badWords()

//NOTE call:  ! usernameAcceptor()  for the validator
function usernameAcceptor(val) {
    if (filter.isProfane(val)) { return JSON.stringify({ Username: 'profane names are not allowed' }) }
    if (val.length < 3) { return JSON.stringify({ Username: 'must be longer than two characters' }) }
    return false
}

class UserModel extends Model {
    constructor() {
        super();
        this.fields = {
            Username: {
                type: 'text',
                rule: {
                    validator: function (value) { return !usernameAcceptor(value) },
                    message: function (value) { return usernameAcceptor(value) }
                }
            },
            Firstname: 'text',
            Surname: 'text',
            Age: 'int',
            Password: {
                type: "text",
                rule: {
                    required: true
                }
            }
        }
        this.key = ['Username']
        this.methods = {
            getName: function () {
                return this.Firstname + " " + this.Surname;
            }
        }
    }
}

var thing = new UserModel()
var obj = {}

for (var k in thing) {
    obj[k] = thing[k]
}
module.exports = obj
module.exports.class = UserModel
