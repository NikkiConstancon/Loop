//levels: { error: 0, warn: 1, info: 2, verbose: 3, debug: 4, silly: 5 }
var winston = require('winston');
require('winston-daily-rotate-file');
var cassandra = require('winston-cassandra').Cassandra;


module.exports = winston;


winston.level = 'debug';


//make log directory once
//https://stackoverflow.com/a/26815894
var fs = require('fs');
var dir = './logs';
if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir);
}

//store logs in db
// winston.add(cassandra, { contactPoints: ['127.0.0.1'], keyspace: 'logs' });

//store logs in file
winston.add(winston.transports.DailyRotateFile, {
    filename: './logs/log',
    datePattern: 'yyyy-MM-dd.',
    prepend: true,
    level: process.env.ENV === 'development' ? 'debug' : 'info'
});

//pretty console colours
winston.cli();



