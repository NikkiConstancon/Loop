var models = require('express-cassandra');
var logger = require('./revaLog');

const modelsDir = __dirname + '/models';
var dbHostAddress = ['127.0.0.1'];//requires array (it would seem)
var dbHostPort = 9042;
var keyspace = 'reva';

const MAX_GLOBAL_TRY_FAILS = 1;
const MAX_TRYS = 10;
const TRYS_INTERVALS = 1000;
var globalTryFails = 0;



var self = module.exports = {
    models: models,///expose express-cassandra
    connected: false,///set to true if connected

    ///The function passed will only exicute if a connection is established.
    ///if not initally connected, intermidiate checks will be made to test if a connection has been made
    /// for a maximum number of 'MAX_TRYS'
    ///if 'MAX_TRYS' is reached and the error callback 'errorcb' was defined, then 'errorcb' will be called.
    try: function(fun) {
        var maxFailHandeler = function () {
            logger.error('databaseManager:try max fails encountered. TODO: notify tech support');
        }
        var context = {};
        var args = arguments;
        if (!self.connected) {
            if (globalTryFails < MAX_GLOBAL_TRY_FAILS) {
                var tiks = MAX_TRYS;
                var timeoutFun = function () {
                    logger.debug('databaseManager:try tick');
                    if (!self.connected) {
                        if (tiks-- > 0) {
                            setTimeout(timeoutFun, TRYS_INTERVALS);
                        } else {
                            globalTryFails++;
                            !(globalTryFails < MAX_GLOBAL_TRY_FAILS) && maxFailHandeler();
                            logger.error('databaseManager:try failed, global fail count: ' + globalTryFails);
                            if (context.errorcb) {
                                context.errorcb();
                            }
                        }
                    } else {
                        fun(args);
                    }
                };
                setTimeout(timeoutFun, TRYS_INTERVALS);
            } else {
                maxFailHandeler();
            }
        }
        return context;
    }
};


//use another keyspace when testing
if (process.argv.indexOf('--test') >= 0) {
    keyspace = 'test'
    logger.warn('databaseManager initialized in testing mode, using keyspace [' + keyspace + ']');
}


//Tell express-cassandra to use the models-directory, and
//use bind() to load the models using cassandra configurations.
models.setDirectory(modelsDir).bind(
    {
        clientOptions: {
            contactPoints: dbHostAddress,
            protocolOptions: { port: dbHostPort },
            keyspace: keyspace,
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
        if (err) logger.error(err.message);
        else {
            self.connected = true;
            logger.info('Cassandra connect to ' + dbHostAddress + ':' + dbHostPort + ' timeuuid:', models.timeuuid());
        }
    }
);



//example usage
/*
function t() {
    var john = new models.instance.users({
        name: "John1",
        surname: "Doe",
        age: 32,
        test: 'oops!'
    });
    john.save(function (err) {
        if (err) {
            console.log(err);
            return;
        }


        models.instance.users.findOne({ name: 'John1' }, function (err, john) {
            if (err) {
                console.log(err);
                return;
            }
            //Note that returned variable john here is an instance of your model,
            //so you can also do john.delete(), john.save() type operations on the instance.
            console.log('Found ' + john.name + ' to be ' + john.age + ' years old!');
        });
    });
};
*/