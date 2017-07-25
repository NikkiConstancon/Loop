var manager = require('./databaseManager');
var util = require('util');


var UserManager = module.exports = function (){
    manager.call(this);
}

util.inherits(UserManager, manager);


UserManager.prototype.addUser = function(user){
	console.log('Adding User');
		new this.models.instance.users(user).save(function(err){
        if(err) {
            console.log(err);
            return;
        }else{
            console.log('User Added Successfully!');
        }
	});
}