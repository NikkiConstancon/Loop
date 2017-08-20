var Model = require('../lib/modelClass')

class ZettaletModel extends Model {
    constructor() {
        super();
        this.fields = {
            Uuid: 'uuid',
            PatientBinding: 'text'
        }
        this.key = ['Uuid']
        this.methods = {
            getPatient: function () {
                return this.Firstname + " " + this.Surname;
            }
        }
    }
}

var thing = new ZettaletModel()
var obj = {}

for (var k in thing) {
    obj[k] = thing[k]
}
module.exports = obj
module.exports.class = ZettaletModel