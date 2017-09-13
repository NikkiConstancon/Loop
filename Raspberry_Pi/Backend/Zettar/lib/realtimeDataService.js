
    const webSockMessenger = require('./webSockMessenger')
    const patientManager = require("../patientManager");

    const logger = require('../revaLog')

    const serviceName = 'RTDS'


    var fromQueueMap = {}


    function pushData(msg) {
        var userSocketContextMap = webSockMessenger.getUserSocketContextMap()
        for (var user in userSocketContextMap) {
            for (var context in userSocketContextMap[user]) {
                var usc = userSocketContextMap[user][context]
                var service = usc.subServiceMap[serviceName]
                service.publish(msg)
            }
        }
    }

    var sevice = module.exports = {
        publish: function (info, response) {
            if (!fromQueueMap[info.from]) {
                fromQueueMap[info.from] = []
                setTimeout(function () {
                    pushData({ [info.from]: fromQueueMap[info.from] })
                    fromQueueMap[info.from] = null
                }, 8)
            }
            fromQueueMap[info.from].push({ [info.name]: response.data.toPrecision(3) })



            //TODO
            /*
            patientManager.getPatient({ Username: info.from }).then(function (pat) {
                var subList = pat.SubscriberList
                for (var i in subList) {
    
                }
            }).catch(function (e) {
                logger.error('@RealTimeDataService$publish:', e)
            })*/
        }
    }

    webSockMessenger.attach(serviceName, {
        connect: function (publisher) {
            var count = 0;
            publisher.publish(count++, function (err) {
            })
        },
        close: function (publisher) {
        },
        sub: function (publisher, obj) {
            publisher.publish(obj)
        }
    })
