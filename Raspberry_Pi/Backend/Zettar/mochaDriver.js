///Using mocha programmatically
///https://github.com/mochajs/mocha/wiki/Using-mocha-programmatically

process.argv.push('--test')//emulate arguments
var logger = require('./revaLog')
var dbMan = require('./databaseManager')
var mailer = require('./lib/mailer')

var Mocha = require('mocha'),
    fs = require('fs'),
    path = require('path');

// Instantiate a Mocha instance.
var mocha = new Mocha();
mocha.timeout(2000)
var testDir = './test'


var willDrop = false
if (process.argv.indexOf('--test-drop') != -1) {
    willDrop = true
    logger.warn('dropping keyspace AFTER test')
}
var keepAlive = false
if (process.argv.indexOf('--test-keepAlive') != -1) {
    keepAlive = true
    logger.warn('keeping test alive')
}


// Add each .js file to the mocha instance
fs.readdirSync(testDir).filter(function(file){
    // Only keep the .js files
    return file.substr(-3) === '.js';

}).forEach(function(file){
    mocha.addFile(
        path.join(testDir, file)
    );
});


logger.info('connectig to Cassandra')
dbMan.try().then(function () {
    logger.level = 'warn';
    // Run the tests.
    mocha.run(function (failures) {
        process.on('exit', function () {
            process.exit(failures);  // exit with non-zero status if there were failures
        });

        if (mailer.pendingSends != 0) {
            console.log('waiting for emails to send')
        }
        var max = 10;
        var exitInterval = setInterval(function () {
            if (mailer.pendingSends == 0 || max-- < 0) {
                if (!keepAlive) {
                    if (willDrop) {
                        logger.warn('now dropping keyspace')
                        dbMan.dropTestKyespaceAndExit();
                    } else {
                        process.exit()
                    }
                }
                clearInterval(exitInterval)
            } else {
                console.log('...')
            }
        }, 1000)
    });
}).catch(function(e){
    logger.level = 'silly'
    logger.error(e)
    process.exit(1)
})

