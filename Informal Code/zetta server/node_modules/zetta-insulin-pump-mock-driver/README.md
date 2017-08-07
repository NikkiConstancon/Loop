# Zetta Insulin Pump Driver

## Install

```
$> npm install zetta-insulin-pump-mock-driver
```

## Usage

```javascript
var zetta = require('zetta');
var Photocell = require('zetta-insulin-pump-mock');

zetta()
  .use(InsulinPump)
  .listen(1337)
```

