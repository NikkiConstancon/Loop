var dbMan = require('./databaseManager');
var util = require('util');
var logger = require('./revaLog');



var patientDataManager = module.exports = {
    addInstance: function (user) {
        dbMan.try(function () {
            new dbMan.models.instance.patientData(user).save(function (err) {
                if (err) {
                    logger.error(err);
                    return;
                } else {
                    logger.debug('Instance Added Successfully!');
                }
            });
        }).errorcb = function () {//optional callback
            logger.error('patientDataManager:addInstance faild');
        };
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