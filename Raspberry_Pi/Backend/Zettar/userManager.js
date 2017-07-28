var dbMan = require('./databaseManager');
var util = require('util');
var logger = require('./revaLog');


var UserManager = module.exports = {
    addUser: function (user) {
        dbMan.try(function() {
            new dbMan.models.instance.users(user).save(function (err) {
                if (err) {
                    logger.error(err);
                    return;
                } else {
                    logger.debug('User Added Successfully!');
                }
            });
        }).errorcb = function() {//optional callback
            logger.error('UserManager:addUser faild');
        };
    }
}
