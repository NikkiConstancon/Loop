/**
 * @file
 * This file is residual and may be removed from the repository
 * though it comes in handy at times to quickly test a new idea
 **/


// var dbManager = require("./databaseManager");
// var dbManager1 = require("./dataManager");
var PatientManager = require("./patientManager");
var subscriberManager = require("./subscriberManager")
var PatientDataManager = require("./patientDataManager")

var CryptoJS = require("crypto-js");

setTimeout(function() {
	console.log("Testing");


    // PatientDataManager.getGraphPoints({
    //    Username: 'nikki'  ,
    //    DeviceId: "thermometer" ,
    //    StartTime:  "2017-09-25 15:16:10.051000+0000",
    //    EndTime:  "2017-09-25 15:16:10.481000+0000",
    // })
    PatientManager.addToDeviceMap(
        {'Username': 'greg'},
        "Kicks", true
    )


  }, 0);
 /*   subscriberManager.addSubscriber({
        Email: "nikkiconstancon@gmail.com",
        Password: CryptoJS.AES.encrypt('Password', 'secret key 123').toString(),
        Relation: "doctor",
        PatientList: []
    })
    subscriberManager.getsubscriber({Email: "nikkiconstancon@gmail.com"})

    PatientManager
                    .addPatient({ 
                        Username: 'nikki',
                        Password: CryptoJS.AES.encrypt('Password', 'secret key 123').toString(),
                        AccessPassword: CryptoJS.AES.encrypt('AccessPassword', 'secret key 123').toString(),
                        SubscriberList : [],
                        Email : "testPatient@test.co.za", 
                        Address : '42 Dale Avenue Hempton 1765',
                        Age : 42, 
                        Weight : 23,
                        Height : 32, 
                        Reason : 'Disability'});*/


     // dbManager1.addUser({name: 'Nikki2', surname: 'Con', age: 21});
     // dbManager2.addInstance({patientName: 'Nikki2', device: 'Con', time: 21});
    // patientDataManager.addInstance({PatientUsername : "name", DeviceID : "data[0].topic", TimeStamp : 0, Value : parseFloat(10.99)  });
   
/*    PatientManager
                    .addPatient({ 
                        Username: 'nikki',
                        Password: CryptoJS.AES.encrypt('Password', 'secret key 123').toString(),
                        AccessPassword: CryptoJS.AES.encrypt('AccessPassword', 'secret key 123').toString(),
                        SubscriberList : [],
                        Email : "testPatient@test.co.za", 
                        Address : '42 Dale Avenue Hempton 1765',
                        Age : 42, 
                        Weight : 23,
                        Height : 32, 
                        Reason : 'Disability'});
          PatientManager
                    .checkUsername("noone");   */        
 /*   subscriberManager.addSubscriber({
        Email: "nikkiconstancon@gmail.com",
        Password: CryptoJS.AES.encrypt('Password', 'secret key 123').toString(),
        Relation: "doctor",
        PatientList: {Username: "nikki", AccessPassword: "AccessPassword"}
    })*/

    // PatientManager.getPatient();