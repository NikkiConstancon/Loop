var express = require('express')
var parseurl = require('parseurl')
var initUserSession = require('./userSession')


var app = express()
initUserSession(app)
server = module.exports = require('http').createServer(app);


server.listen(3000, function (err) {
    err && console.log(err)
    console.log('\tserver started ', server.address())
})



/*
require('uuid/v1');

var net = require('net');


var HOST = '127.0.0.1'; // parameterize the IP of the Listen
var PORT = 6969; // TCP LISTEN port


// Create an instance of the Server and waits for a conexão
net.createServer(function(sock) {


  // Receives a connection - a socket object is associated to the connection automatically
  console.log('CONNECTED: ' + sock.remoteAddress +':'+ sock.remotePort);


  // Add a 'data' - "event handler" in this socket instance
  sock.on('data', function(data) {
	  // data was received in the socket 
	  // Writes the received message back to the socket (echo)
      sock.write(data);
      console.log(data)
  });


  // Add a 'close' - "event handler" in this socket instance
  sock.on('close', function(data) {
	  // closed connection
	  console.log('CLOSED: ' + sock.remoteAddress +' '+ sock.remotePort);
  });

  sock.on('error', function(data) {
	  // closed connection
      console.log(data)
      console.log('error: ' + sock.remoteAddress +' '+ sock.remotePort);
  });

}).listen(PORT, HOST);


console.log('Server listening on ' + HOST +':'+ PORT);
*/














/*

var tls = require('tls'),
    fs = require('fs'),
    colors = require('colors'),
    msg = [
        ".-..-..-.  .-.   .-. .--. .---. .-.   .---. .-.",
        ": :; :: :  : :.-.: :: ,. :: .; :: :   : .  :: :",
        ":    :: :  : :: :: :: :: ::   .': :   : :: :: :",
        ": :: :: :  : `' `' ;: :; :: :.`.: :__ : :; ::_;",
        ":_;:_;:_;   `.,`.,' `.__.':_;:_;:___.':___.':_;"
    ].join("\n").cyan;

var options = {
    key: fs.readFileSync('private-key.pem'),
    cert: fs.readFileSync('public-cert.pem')
};

tls.createServer(options, function (s) {
    s.write(msg + "\n");
    s.pipe(s);
}).listen(8000);
*/