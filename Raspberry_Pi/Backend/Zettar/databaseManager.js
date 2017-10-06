/**
 * @file
 * This file contained the singleton database manager that will provide a single point 
 * to the database for continence and better error handling
 *
 * @arg --test will initiate the database on the testing keyspace [test] to avoid 
 * collision with the production keyspace [reva]
 **/

var models = require('express-cassandra');
var logger = require('./revaLog');

const modelsDir = __dirname + '/models';
var dbHostAddress = ['127.0.0.1'];//requires array (it would seem)
var dbHostPort = 9042;
var keyspace = 'reva';

const MAX_GLOBAL_TRY_FAILS = 1;
const MAX_TRYS = 20;
const TRYS_INTERVALS = 3000;
var globalTryFails = 0;



var self = module.exports = {
    models: models,///expose express-cassandra
    connected: false,///set to true if connected

    /**
    @brief: as a promise, will ensure a connectd db. Will make @param retry number of attempts to connect if no connection
        is established
    @TODO: handle faliurs, e.g. notify tech support etc.
    @return: a promise
    **/
    try: (retry, intervals) => {
        return new Promise(function (resolve, reject) {
            var maxFailHandeler = function () {
                msg = '#databaseManager#try max fails encountered. TODO: notify tech support';
                logger.error(msg)
                reject(msg)
            }
            if (!self.connected) {
                if (globalTryFails < MAX_GLOBAL_TRY_FAILS) {
                    var tiks = retry || MAX_TRYS
                    var timeoutFun = function () {
                        logger.debug('#databaseManager#try tick')
                        if (!self.connected) {
                            if (tiks-- > 0) {
                                setTimeout(timeoutFun, TRYS_INTERVALS)
                            } else {
                                globalTryFails++
                                !(globalTryFails < MAX_GLOBAL_TRY_FAILS) && maxFailHandeler()
                                var msg = '#databaseManager#try failed, global fail count: ' + globalTryFails;
                                logger.error(msg)
                                reject(msg)
                            }
                        } else {
                            resolve(self)
                        }
                    };
                    setTimeout(timeoutFun, intervals || TRYS_INTERVALS)
                } else {
                    maxFailHandeler()
                }
            } else {
                resolve(self)
            }
        })
    },
    getKeyspcaeName: function () {
        return keyspace
    },
    /**
     * @brief drop the test keyspace and exit the program
     **/
    dropTestKyespaceAndExit: function (exitParam) {
        var exit = function () {
            if (exitParam) {
                process.exit(exitParam)
            } else {
                process.exit()
            }
        }
        if (keyspace === 'test') {
            const cassandra = require('cassandra-driver')
            var client = new cassandra.Client({ contactPoints: dbHostAddress, keyspace: keyspace, socketOptions:{readTimeout: 10 } })
            client.connect(function (err, result) {
                if (err) {
                    //logger.error(err)
                    exit()
                } else {
                    client.execute('drop keyspace ' + keyspace).then(function () {
                        //logger.debug('here')
                        exit()
                    }).catch(function (e) {
                        //logger.error(e)
                        exit()
                    })
                }
            })
        }
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
        if (err) logger.error('#DatabaseManager: ls' + err.message);
        else {
            self.connected = true;
            logger.info('Cassandra connect to ' + dbHostAddress + ':' + dbHostPort + ' timeuuid:', models.timeuuid());
        }
    }
);


/**
 *@example usage
 *
 *function t() {
 *    var john = new models.instance.users({
 *        name: "John1",
 *        surname: "Doe",
 *        age: 32,
 *        test: 'oops!'
 *    });
 *    john.save(function (err) {
 *        if (err) {
 *            console.log(err);
 *            return;
 *        }
 *
 *
 *        models.instance.users.findOne({ name: 'John1' }, function (err, john) {
 *            if (err) {
 *                console.log(err);
 *                return;
 *            }
 *            //Note that returned variable john here is an instance of your model,
 *            //so you can also do john.delete(), john.save() type operations on the instance.
 *            console.log('Found ' + john.name + ' to be ' + john.age + ' years old!');
 *        });
 *    });
 *};
 **/