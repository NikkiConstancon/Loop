var UserManager =  function (){
	this.models = require('express-cassandra');

// function init(cb){

        console.log('Initializer');
        //Tell express-cassandra to use the models-directory, and
        //use bind() to load the models using cassandra configurations.
        this.models.setDirectory('/home/nikki/Documents/301Project/DataBaseScrap' + '/models').bind(
            {
                clientOptions: {
                    contactPoints: ['127.0.0.1'],
                    protocolOptions: { port: 9042 },
                    keyspace: 'reva',
                    queryOptions: {consistency: this.models.consistencies.one}
                },
                ormOptions: {
                    //If your keyspace doesn't exist it will be created automatically
                    //using the default replication strategy provided here.
                    defaultReplicationStrategy : {
                        class: 'SimpleStrategy',
                        replication_factor: 1
                    },
                    migration: 'safe',
                    createKeyspace: true
                }
            },
            function(err) {
                if(err) console.log(err.message);
                else   
                 {
                    console.log('End Initializer');
                    
                }
            }
        );
}

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

module.exports = {
	manager: UserManager
}