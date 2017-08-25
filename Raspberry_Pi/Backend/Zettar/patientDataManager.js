'use strict';

var dbMan = require('./databaseManager');
var util = require('util');
var logger = require('./revaLog');


var patientDataManager = module.exports = {
    addInstance: function (user){
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                var newUser = new dbMan.models.instance.patientData(user)
                newUser.save(function (err) {
                    if (err) {
                        logger.error(err)
                        reject(err)
                    } else {
                        logger.debug('Instance Added Successfully!')
                        resolve(newUser)
                    }
                })
            }).catch((err)=>{
                reject(err)
            })
        })
    },

    getInstance: function (name) {
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                dbMan.models.instance.patientData.findOne({ name: name }, function (err, found) {
                    if (err) {
                        logger.error(err)
                        reject(err)
                    }
                    //Note that returned variable john here is an instance of your model,
                    //so you can also do john.delete(), john.save() type operations on the instance.
                    logger.debug('Found Instance: ' + found.name)
                    resolve(found)
                })
            }).catch((err) => {
                reject(err)
            })
        })
    }
}

/*
var patientDataManager = module.exports = function (){
    manager.call(this);
}

util.inherits(patientDataManager, manager);


patientDataManager.prototype.addInstance = function(data){
	console.log('Adding Data');
		new this.models.instance.patientdata(user).save(function(err){
        if(err) {
            console.log(err);
            return;
        }else{
            console.log('Data Added Successfully!');
        }
	});
}

*/