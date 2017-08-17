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



module.exports = new UserModel()
module.exports.class = UserModel
