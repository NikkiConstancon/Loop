var Model = require('../lib/modelClass')

class UserModel extends Model {
    constructor() {
        super();
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
}

var thing = new UserModel()
var obj = {}

for (var k in thing) {
    obj[k] = thing[k]
}
module.exports = obj
module.exports.class = UserModel
