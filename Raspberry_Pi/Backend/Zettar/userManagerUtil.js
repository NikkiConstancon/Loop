
var logger = require('./revaLog');


var UserManager = module.exports = {
    addPubSubRequestAsRequester: function (userUid, passCb, failCb) {
        var map = this.PubSubBindingConfirmationMap || {}
        var info = {}
        info.type = UserManager.enum.pubSubReq.type.request
        info.state = UserManager.enum.pubSubReq.state.pending
        map[userUid] = JSON.stringify(info)
        this.PubSubBindingConfirmationMap = map
        this.save(function (err) {
            if (err) failCb && failCb({systemError: err });
            else passCb && passCb(info)
        });
    },
    addPubSubRequestAsTarget: function (userUid, passCb, failCb) {
        var ffs = this.getPassword()//WTF?? if I dont do this it complains that Password has to be set!
        var map = this.PubSubBindingConfirmationMap || {}
        var info = {}
        info.type = UserManager.enum.pubSubReq.type.target
        info.state = UserManager.enum.pubSubReq.state.pending
        map[userUid] = JSON.stringify(info)
        this.PubSubBindingConfirmationMap = map
        this.save(function (err) {
            if (err) failCb && failCb({systemError: err })
            else passCb && passCb(info)
        });
    },
    pubSubRequestOnDecision: function (onUserUid, decision) {
        //NOTE: just delete the key value pair for now to minimize serve client state change handshaking
        try {
            var map = this.PubSubBindingConfirmationMap
            if (map && !map[onUserUid]) {
                return false
            }
            var obj = JSON.parse(map[onUserUid])
            if (decision) {
                if (obj.type == UserManager.enum.pubSubReq.type.target) {
                    //TODO add to subList
                } else {
                    //TODO add to patList
                    obj.state = UserManager.enum.pubSubReq.state.accepted
                }
            } else {
                obj.state = UserManager.enum.pubSubReq.state.declined
            }
            map[onUserUid] = JSON.stringify(obj)
            delete map[onUserUid]//allways for now
            this.PubSubBindingConfirmationMap = map
            this.save()
            return true
        } catch (e) {
            logger.error(e)
        }
        return false
    }
}

UserManager.enum = {
    userType: {
        patient: "patient",
        subscriber: "subscriber"
    },
    pubSubReq: {
        type: {
            request: "request",
            target: "target"//i.e. patient
        },
        state: {
            pending: "pending",
            delivered: "delivered",
            declined: "declined",
            accepted: "accepted"
        }
    }
}
