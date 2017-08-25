'use strict';
/**
 * @file
 * This file implements a singleton design pattern to allow easy access to users of the system.
 * It also simplifies database queries and provides error correction in events that such capabilities
 * are needed.
 **/


var dbMan = require('./databaseManager');
var util = require('util');
var logger = require('./revaLog');


var UserManager = module.exports = {
    /**
     *@brief adds a new user to the database
     *
     *@return a promise passing the newly added user to the function called back
     */
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
    /**
     *@brief gets a user to the database
     *
     *@return a promise passing the newly added user to the function called back
     */
    getUser: function (name) {
        return new Promise((resolve, reject) => {
            dbMan.try().then(function () {
                dbMan.models.instance.users.findOne({ Username: name }, function (err, found) {
                    if (err) {
                        logger.error(err)
                        reject(err)
                    }
                    //Note that returned variable john here is an instance of your model,
                    //so you can also do john.delete(), john.save() type operations on the instance.
                    logger.debug('Found user: ' + found.Username)
                    resolve(found)
                })
            }).catch((err) => {
                reject(err)
            })
        })
    }
}
