var zetta = require('zetta');
var display = require('./display.js');

// var LevelRegistries = require('zetta-leveldb-registry')(zetta);
// var PeerRegistry = LevelRegistries.PeerRegistry;
// var DeviceRegistry = LevelRegistries.DeviceRegistry;

//{ registry: new DeviceRegistry(), peerRegistry: new PeerRegistry()}

zetta()
  .name('Zettar')
  // .use(display)
  .listen(3009, function(){
    console.log('Zettar is running : 3009');

});