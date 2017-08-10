var zetta = require('zetta');
var url = require('url');
// var display = require('./display.js');
var Hook = require('./lib/zettaHook')

//init zetta as usual, but dont call link yet
//  the listen call is deferred to the hook
var initializedZetta = zetta('peers').name('Zettar')

//pass the initialized zetta var to a new hook
var hook = new Hook(initializedZetta)
    //call listen as you wold on zetta
    .listen(3009, function () {
        console.log('Zettar is running : 3009');
    })
    //here you hook the streams
    .registerStreamListener({
        topicName: 'value',
        where: { type: 'state_machine', name: 'heart_monitor' },
        cb: function (info, data) {
            console.log(info)
            console.log(data)
        },
        errcb: function (e) {
            console.log(e)
        }
    })
    .registerStreamListener({
        topicName: 'value',
        where: { type: 'state_machine', name: 'temp_monitor' },
        cb: function (info, data) {
            console.log(info)
            console.log(data)
        },
        errcb: function (e) {
            console.log(e)
        }
    })
    .registerStreamListener({
        topicName: 'concentration',
        where: { type: 'glucose-meter'},
        cb: function (info, data) {
            console.log(info)
            console.log(data)
        },
        errcb: function (e) {
            console.log(e)
        }
    })
    .registerStreamListener({
        topicName: 'concentration',
        where: { type: 'insulin-pump'},
        cb: function (info, data) {
            console.log(info)
            console.log(data)
        },
        errcb: function (e) {
            console.log(e)
        }
    })
    .registerStreamListener({
        topicName: 'temperature',
        where: { type: 'thermometer'},
        cb: function (info, data) {
            console.log(info)
            console.log(data)
        },
        errcb: function (e) {
            console.log(e)
        }
    })
  