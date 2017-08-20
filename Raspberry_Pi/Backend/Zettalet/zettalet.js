var zetta = require('zetta');
var scout = require('./scout.js');
var display = require('./display.js');
var keys = require('../Shared/keys')

uuidv1 = require('uuid/v1')

const arr = new Array(8)
uuidv1(null, arr, 8)

var uuid = new Buffer(arr).toString('base64')
var fs = require('fs');
var dir = './.data/'
var filename = dir + 'name'
try {
    uuid = fs.readFileSync(filename, 'utf8').toString();
} catch (e) {
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir)
    }
    fs.writeFileSync(filename, uuid)
}






var port = Math.floor(Math.random() * (65535 - 1024) + 1024)
var linkAddress = 'http://197.242.150.255:3009'
if (process.argv.indexOf('--test') >= 0) {
    linkAddress = 'http://127.0.0.1:3009'
    console.log('Zetalet initialized in testing mode, using link address [' + linkAddress + ']')
}

zetta()
    .name(uuid)
	.use(scout)
	.use(display)
    .link(linkAddress)
    .listen(port, function(){
	});
