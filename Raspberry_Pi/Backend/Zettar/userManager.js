'use strict';

var dbMan = require('./databaseManager');
var util = require('util');
var logger = require('./revaLog');


var UserManager = module.exports = {
    addUser: function (user){
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                var newUser = new dbMan.models.instance.users(user)
                newUser.save(function (err) {
                    if (err) {
                        logger.error(err)
                        reject(err)
                    } else {
                        logger.debug('User Added Successfully!')
                        resolve(newUser)
                    }
                })
            }).catch((err)=>{
                reject(err)
            })
        })
    },

    getUser: function (name) {
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                dbMan.models.instance.users.findOne({ name: name }, function (err, found) {
                    if (err) {
                        logger.error(err)
                        reject(err)
                    }
                    //Note that returned variable john here is an instance of your model,
                    //so you can also do john.delete(), john.save() type operations on the instance.
                    logger.debug('Found user: ' + found.name)
                    resolve(found)
                })
            }).catch((err) => {
                reject(err)
            })
        })
    }
}
