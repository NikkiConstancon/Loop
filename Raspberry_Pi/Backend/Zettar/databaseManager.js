var databaseManager = module.exports = function (){
	this.models = require('express-cassandra');


    console.log('Initializer');
    //Tell express-cassandra to use the models-directory, and
    //use bind() to load the models using cassandra configurations.

    models.setDirectory('/home/nikki/Documents/301/EPI-USE_PROJECT/githubRepo/Loop/Raspberry_Pi/Backend/Zettar/models').bind(
        {
            clientOptions: {
                contactPoints: ['127.0.0.1'],
                protocolOptions: { port: 9042 },
                keyspace: 'reva',
                queryOptions: {consistency: models.consistencies.one}
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
            else    {
                console.log(models.timeuuid());
            }
        }
    );
        console.log("End Initializer");
}