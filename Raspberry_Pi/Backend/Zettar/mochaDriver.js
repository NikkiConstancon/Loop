///Using mocha programmatically
///https://github.com/mochajs/mocha/wiki/Using-mocha-programmatically

process.argv.push('--test');//emulate arguments
var logger = require('./revaLog');
var dbMan = require('./databaseManager');
logger.level = 'warn';

var Mocha = require('mocha'),
    fs = require('fs'),
    path = require('path');

// Instantiate a Mocha instance.
var mocha = new Mocha();
mocha.timeout(20000)
var testDir = './test'

// Add each .js file to the mocha instance
fs.readdirSync(testDir).filter(function(file){
    // Only keep the .js files
    return file.substr(-3) === '.js';

}).forEach(function(file){
    mocha.addFile(
        path.join(testDir, file)
    );
});

// Run the tests.
mocha.run(function(failures){
  process.on('exit', function () {
      process.exit(failures);  // exit with non-zero status if there were failures
    });
  dbMan.dropTestKyespaceAndExit();
});

