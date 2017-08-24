/**
 * @file
 * This is a test file in development that will dispatch events from the server to the client
 */
const webSockMessenger = require('./webSockMessenger')



var subscribersMap = {}

webSockMessenger.attatch('ZettaletEvent', {
    connect: function (user, send) {
        console.log(user)
        var ival = setInterval(function () {
            send('pushing to you', function (err) {
                err && clearInterval(ival)
            });
        }, 1000)
    },
    close: function (user) {
        console.log(user, ' has been')
    },
    sub: function (user, obj) {
        console.log(user, obj)
    }
})


module.exports = {
}