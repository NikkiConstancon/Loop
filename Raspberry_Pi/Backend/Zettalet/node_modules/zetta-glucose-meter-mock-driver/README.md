# Zetta Glucose Meter Mock Driver

## Install

```
$> npm install zetta-glucose-meter-mock-driver
```

## Usage

```javascript
var zetta = require('zetta');
var Photocell = require('zetta-glucose-meter-mock');

zetta()
  .use(GlucoseMeter)
  .listen(1337)
```

