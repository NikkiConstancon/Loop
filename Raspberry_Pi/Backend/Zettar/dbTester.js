// var dbManager = require("./databaseManager");
// var dbManager1 = require("./dataManager");
var PatientManager = require("./patientManager");

var CryptoJS = require("crypto-js");

setTimeout(function() {
	console.log("Testing");
     // dbManager1.addUser({name: 'Nikki2', surname: 'Con', age: 21});
     // dbManager2.addInstance({patientName: 'Nikki2', device: 'Con', time: 21});
    // patientDataManager.addInstance({PatientUsername : "name", DeviceID : "data[0].topic", TimeStamp : 0, Value : parseFloat(10.99)  });
   
    PatientManager
                    .removeFromSubscriberList({ 
                        Username: 'Username_test',
                        Password: CryptoJS.AES.encrypt('Password', 'secret key 123').toString(),
                        AccessPassword: CryptoJS.AES.encrypt('AccessPassword', 'secret key 123').toString(),
                        SubscriberList : [],
                        Email : "testPatient@test.co.za", 
                        Address : '42 Dale Avenue Hempton 1765',
                        Age : 42, 
                        Weight : 23,
                        Height : 32, 
                        Reason : 'Disability'},"test1@test.com");

    // PatientManager.getPatient();
  }, 0);
