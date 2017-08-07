var util = require('util');
var extend = require('node.extend');

var IMAGE_URL_ROOT = 'http://www.zettaapi.org/icons/';
var IMAGE_EXTENSION = '.png';

var stateImageForDevice = function(device) {
  return IMAGE_URL_ROOT + device.type + '-' + device.state + IMAGE_EXTENSION;
}

module.exports = function(server) {
  // TODO: swap with server.ql and text
  var automobileQuery = server.where({type: 'automobile'});
  server.observe([automobileQuery], function(automobile) {
    automobile.style = extend(true, automobile.style, {properties: {}});
    automobile.style.properties = extend(true, automobile.style.properties, {
      vehicleSpeed: {
        display: 'billboard',
        significantDigits: 1,
        symbol: 'km/h'
      }
    });
    var states = Object.keys(automobile._allowed);
    for (i = 0; i < states.length; i++) {
      automobile._allowed[states[i]].push('_update-state-image');
    }
    automobile._transitions['_update-state-image'] = {
      handler: function(imageURL, tintMode, foregroundColor, cb) {
        if (tintMode !== 'original') {
          tintMode = 'template';
        }
        automobile.style.properties = extend(true, automobile.style.properties, {
          stateImage: {
            url: imageURL,
            tintMode: tintMode
          }
        });
        if (foregroundColor) {
          automobile.style.properties.stateImage.foregroundColor = foregroundColor;
        }
        cb();
      },
      fields: [
        {name: 'imageURL', type: 'text'},
        {name: 'tintMode', type: 'text'},
        {name: 'foregroundColor', type: 'text'}
      ]
    };
    automobile.call('_update-state-image', stateImageForDevice(automobile), 'template', null);
    var stateStream = automobile.createReadStream('state');
    stateStream.on('data', function(newState) {
      automobile.call('_update-state-image', stateImageForDevice(automobile), 'template', null);
    });
    automobile.style.actions = extend(true, automobile.style.actions, {'_update-state-image': {display: 'none'}});
  });
}