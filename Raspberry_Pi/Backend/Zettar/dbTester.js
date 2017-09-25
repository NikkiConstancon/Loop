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
    PatientDataManager.getGraphPoints({
       Username: ''  ,
       DeviceId: "thermometer/2f15a253-46c1-45c3-b940-291afe275537/vitals" ,
       StartTime:  "2017-09-12 18:14:55.391000+0000",
       EndTime:  "2017-09-12 18:14:57.503000+0000",
       Interval: 1
    })

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