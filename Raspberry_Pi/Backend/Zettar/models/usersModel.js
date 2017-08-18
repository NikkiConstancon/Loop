var Model = require('../lib/modelClass')

function UserModel() {
        this.fields = {
            Username: 'text',
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
UserModel.prototype = Model
var thing = new UserModel()
var obj = {}

for (var k in thing) {
    obj[k] = thing[k]
}
module.exports = obj
module.exports.class = UserModel
