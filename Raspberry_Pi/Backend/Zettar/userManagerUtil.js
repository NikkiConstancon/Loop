


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
            else passCb && passCb()
        });
    },
    addPubSubRequestAsTarget: function (userUid, passCb, failCb) {
        var ffs = this.getPassword()//WTF?? if I dont do this it complains that Password has to be set!
        var map = this.PubSubBindingConfirmationMap || {}
        var info = {}
        info.type = UserManager.enum.pubSubReq.type.request
        info.state = UserManager.enum.pubSubReq.state.pending
        map[userUid] = JSON.stringify(info)
        this.PubSubBindingConfirmationMap = map
        this.save(function (err) {
            if (err) failCb && failCb({systemError: err })
            else passCb && passCb()
        });
    }
}

UserManager.enum = {
    pubSubReq: {
        type: {
            request: "request",
            target: "target"
        },
        state: {
            pending: "pending",
            delivered: "delivered",
            confirmed: "confirmed"
        }
    }
}
