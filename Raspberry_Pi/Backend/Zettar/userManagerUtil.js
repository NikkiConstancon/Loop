


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
        info.type = UserManager.enum.pubSubReq.type.request
        info.state = UserManager.enum.pubSubReq.state.pending
        map[userUid] = JSON.stringify(info)
        this.PubSubBindingConfirmationMap = map
        this.save(function (err) {
            if (err) failCb && failCb({systemError: err })
            else passCb && passCb(info)
        });
    },
    addToSubscriberList: function(_newPatient){
         var updateValue;

        if (this.SubscriberList == null) {
            updateValue = [_newPatient]
        } else {
            updateValue = this.SubscriberList
            if (updateValue.indexOf(_newPatient) > -1) {
                //already on list
                return false;
            } else{
                updateValue.push(_newPatient)
                //store updated value
                console.log(updateValue)
                
                //Check if password is correct:
                validatePatient(_newPatient);

                this.SubscriberList = updateValue;
                this.save(function(err){return true})
            }
        }
    }
    addToPatientList: function(_newPatient){
        var updateValue;

        if (this.PatientList == null) {
            updateValue = [_newPatient]
        } else {
            updateValue = this.PatientList
            if (updateValue.indexOf(_newPatient) > -1) {
                //already on list
                return false;
            } else{
                updateValue.push(_newPatient)
                //store updated value
                console.log(updateValue)

                //Check if password is correct:
                validatePatient(_newPatient);

                this.PatientList = updateValue;
                this.save(function(err){return true})
            }
        }
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
