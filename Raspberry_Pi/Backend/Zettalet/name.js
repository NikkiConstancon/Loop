uuidv1 = require('uuid/v1')
keys = require('../Shared/sharedKeys')

const arr = new Array(8)
uuidv1(null, arr, 8)

var name = new Buffer(arr).toString('base64')
var fs = require('fs');
var dir = './.data/'
var filename = dir + 'name'
try {
    name = fs.readFileSync(filename, 'utf8').toString();
} catch (e) {
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir)
    }
    fs.writeFileSync(filename, name)
}



console.log('I am: "' + name + '"')
module.exports = keys.encrypt(name)
