// var dbManager = require("./databaseManager");
var dbManager1 = require("./userManager");
var dbManager2 = require("./patientDataManager");

dbManager1();
// dbManager2();

setTimeout(function() {
	console.log("Testing");
    // dbManager1.manager.addUser({name: 'Nikki', surname: 'Con', age: 21});
    // dbManager2.addInstance({patientName: 'Nikki', device: 'Con', time: 21});

  }, 5000);
