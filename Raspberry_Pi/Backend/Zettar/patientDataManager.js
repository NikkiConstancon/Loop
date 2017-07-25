var manager = require('./databaseManager');
var util = require('util');

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