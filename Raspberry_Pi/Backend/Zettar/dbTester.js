// var dbManager = require("./databaseManager");
var dbManager1 = require("./userManager");
var patientDataManager = require("./patientDataManager");

setTimeout(function() {
	console.log("Testing");
     // dbManager1.addUser({name: 'Nikki2', surname: 'Con', age: 21});
     // dbManager2.addInstance({patientName: 'Nikki2', device: 'Con', time: 21});
    patientDataManager.addInstance({PatientUsername : "name", DeviceID : "data[0].topic", TimeStamp : 0, Value : parseFloat(10.99)  });


  }, 1000);
