var zetta = require('zetta');
var url = require('url');
// var display = require('./display.js');

zetta()
	.name('Zettar')
	// .use(display)
	.listen(3009, function(){
    	console.log('Zettar is running : 3009');
	});
  