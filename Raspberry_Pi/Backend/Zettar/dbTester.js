/**
 * @file
 * This file is residual and may be removed from the repository
 * though it comes in handy at times to quickly test a new idea
 **/


// var dbManager = require("./databaseManager");
// var dbManager1 = require("./dataManager");
var patientManager = require("./patientManager");
var subscriberManager = require("./subscriberManager")
var PatientDataManager = require("./patientDataManager")

var CryptoJS = require("crypto-js");

setTimeout(function() {
	console.log("Testing");

    //Test Patient
    // PatientManager.addPatient()
    //console.log(PatientDataManager.getInstance("greg"));
    patientManager.getDeviceMap({ Username: 'greg' }).then(function (pat) { 
        PatientDataManager.getGraphPoints({
            Username: 'greg'  ,
            StartTime:  parseFloat(1407101299972),
            EndTime:  parseFloat(1607101300500),
        }).then(function(result){
            
            
            
            //console.log(result);
            
            var endResult = []
            for(var i= 0; i < Object.keys(pat).length - 1; i ++){
                    if(pat[Object.keys(pat)[i]] == true){
                        var subResult = [];
                        // add device type
                        subResult.push({deviceID: Object.keys(pat)[i]});
                        //go through result...
                        for(var j = 0; j <  Object.keys(result).length - 1; j++){
                            //console.log("Device: " + result[Object.keys(result)[j]].device)
                            //console.log("Compare: " + Object.keys(pat)[i])
                            if(result[Object.keys(result)[j]].device == Object.keys(pat)[i]){
                                //console.log("here: ");
                                //console.log(result[Object.keys(result)[j]].device)
                                subResult.push({x: result[Object.keys(result)[j]].x, y: result[Object.keys(result)[j]].y});
                            }
                        }
                        endResult.push(subResult);
                        
                        
                    }
            }
            console.log(endResult);
            
            
        })
    })
    
     /*PatientManager.addToDeviceMap(
         {'Username': 'greg'},
         "Kicks", true
     )*/
    //(patientManager.getPatient({Username:"greg"})).then(function(hu){hu.addToPatientList("NEW1")});
    // (subscriberManager.getsubscriber({Email:"what@sub.com"})).then(function(hu){hu.addToPatientList("rinus")})
    // Patient.addToPatientList("no.@e");
    


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
