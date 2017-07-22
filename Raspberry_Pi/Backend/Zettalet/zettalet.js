var zetta = require('zetta');
var scout = require('./scout.js');
var display = require('./display.js');


zetta()
	.name('Stream Device Server')
	.use(scout)
	.use(display)
	.link('http://127.0.0.1:3009')
	.listen(6000, function(){
    	console.log('Zettelet is running: 6000');
	});
