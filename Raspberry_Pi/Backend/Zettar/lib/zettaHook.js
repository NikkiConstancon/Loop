//NOTE: module.exports is at end of file
var logger = require('../revaLog')

//====================BEGIN CLASS Hook====================
function Hook(zetta) {
    this.name = 'RevaZettaHook'
    this.zetta = zetta
    this.streamListenerArr = []

    var self = this;
    zetta.pubsub.emitter.on('_data', function (key, obj) {
        self.dataHandeler(key, obj)
    })
}
//--------------------methods--------------------

///wrapper for #listenZettaUpdates
///@brief regester handelers for spesific device values, use as zetta's server.where clause to define query
///@arg args {
///  topicName (non optional): value to hook
///  where (optional object): the server.where claus for the query
///}
///@arg cb (non optional): the callback to bind in the form: function(info, data){}
///@arg errcb (optional): an error callback if invalid topicName was passed
///
///@example 
///    hook.registerStreamListener({
///        topicName: 'value',
///            where: { type: 'state_machine', name: 'heart_monitor' },
///        cb: function (info, data) {
///            console.log(info)
///            console.log(data)
///        },
///        errcb: function (e) {
///            console.log(e)
///        }
///    })
Hook.prototype.registerStreamListener = function (args) {
    this.streamListenerArr.push(args)
    return this
}
Hook.prototype.listen = function (port, cb) {
    var self = this;
    self.zetta
        .use(function (server) {
            self.fBootstrapped = true;
            self.server = server;
        })
        .listen(port, cb)
    return this
}
Hook.prototype.dataHandeler = function (key, obj) {
    var fun = dataHandelerMap[key]
    if (fun) {
        fun.call(this, obj)
    }
    return this
}
//function manifold map used by: Hook.prototype.dataHandeler
//all functions will(must) be called with the this context set to an instance of Hook
var dataHandelerMap = {
    '_peer/connect': function (obj) {
        //console.log(obj)
        //console.log('connect')
        for (var i in this.streamListenerArr) {
            var params = this.streamListenerArr[i]
            params.server = this.server
            params.from = obj.peer.name
            listenZettaUpdates(params)
        }
    },
    '_peer/disconnect': function (obj) {
        //console.log('disconnect')
        //console.log(obj)
    }
}

//====================END CLASS Hook====================



//====================BEGIN STATIC UTILS====================


//@arg args {
//  server (non optional): the server returned when calling zetta().use(function (server) {})
//  topicName (non optional): value to hook
//  where (optional object): the server.where claus for the query
//}
//@arg cb (non optional): the callback to bind in the form function(@param args, obj)
//@arg errcb (optional): an error callback if invalid topicName was passed
var listenZettaUpdates = function (args) {
    try {
        if (!args.server) {
            throw new Error('#listenZettaStream: server argument must be defined')
        }
        if (!args.topicName) {
            throw new Error('#listenZettaStream: topicName argument must be defined')
        }
        if (!args.cb) {
            throw new Error('#listenZettaStream: callback argument cb must be defined')
        }
        var server = args.server
        var from = args.from || '*'
        var topicName = args.topicName
        var where = args.where || {}
        var cb = args.cb
        var errcb = args.errcb
        server.observe([server.from(from).where(where)], function (thing) {
            try {
                //capture info
                var info = { from: from, topicName: topicName, where: where }
                var key = buildObserveKeyForHook(thing, topicName)
                logger.debug('#listenZettaStream: key:', key)
                hookObserveEmiter(thing, key, function () {
                    cb(info, arguments)
                })
            } catch (e) {
                logger.debug('#listenZettaStream', e)
                errcb && errcb(e)
            }
        })
    } catch (e) {
        logger.debug('#listenZettaStream', e)
        throw e
    }
}
var buildObserveKeyForHook = function (thing, topicName) {
    return thing.type + '/' + thing.id + '/' + topicName
}
var hookObserveEmiter = function (thing, emiterKey, cb) {
    if (!thing._socket._events[emiterKey]) {
        throw new Error('#hookZettaEmiter: invalid emiterKey ' + emiterKey)
    }
    thing._socket._events[emiterKey] = cb
}
//====================END STATIC UTILS====================




module.exports = Hook