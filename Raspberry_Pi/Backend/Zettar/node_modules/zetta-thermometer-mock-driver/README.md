# Zetta Thermometer Mock Driver

## Install

```
$> npm install zetta-thermometer-mock-driver
```

## Usage

```javascript
var zetta = require('zetta');
var Thermometer = require('zetta-thermometer-mock');

zetta()
  .use(Thermometer)
  .listen(1337)
```

