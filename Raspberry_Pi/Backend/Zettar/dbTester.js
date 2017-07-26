// var dbManager = require("./databaseManager");
var dbManager1 = require("./userManager");
var dbManager2 = require("./patientDataManager");

setTimeout(function() {
	console.log("Testing");
     dbManager1.addUser({name: 'Nikki2', surname: 'Con', age: 21});
     dbManager2.addInstance({patientName: 'Nikki', device: 'Con', time: 21});

  }, 1000);
