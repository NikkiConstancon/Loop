var zetta = require('zetta');
var scout = require('./scout.js');

zetta()
  .name('Stream Device Server')
  //.use(style)
  .use(scout)
  .listen(1337, function(){
     console.log('Zetta is running at http://127.0.0.1:1337');
});
