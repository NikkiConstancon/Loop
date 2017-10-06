/**
 * @file
 * This file is residual and may be removed from the repository
 * though it comes in handy at times to quickly test a new idea
 **/


// var dbManager = require("./databaseManager");
// var dbManager1 = require("./dataManager");
var patientManager = require("./patientManager");
var subscriberManager = require("./subscriberManager")
var dataManager = require("./patientDataManager")

var CryptoJS = require("crypto-js");

function compress(StartTime, EndTime, endResult){
    var result = {};
    for(var device in endResult){ //compress each device
       // console.log("device ID: " + device);
        result[device] = [];
       // console.log("Start time : " + StartTime);
       // console.log("End time : " + EndTime);
        var segment = (EndTime - StartTime ) / 6;
       // console.log("Segment: " + segment);
        var midEnd = StartTime;
        var midStart = StartTime;
        while(midStart < EndTime){ //for each segment that is there
            midEnd += segment;
          //  console.log("Compress: " + midStart + "  " + midEnd);
            var avgX = 0;
            var avgY = 0;
            var count = 0;
            var minMaxAvg = {};
            //console.log("device")
            //console.log(endResult[device]);
            for(var key in endResult[device]){
                //console.log("KEY" + key);
               // console.log("end")
               // console.log(endResult[device][key]);
                if(endResult[device][key].x > midStart && endResult[device][key].x < midEnd){
                   // console.log("TRUE" );
                    //key in range   
                    count ++;
                    avgX += endResult[device][key].x;
                    avgY += endResult[device][key].y;
                }else if(endResult[device][key].Min) {
                    //console.log("HERE");
                    //console.log(endResult[device][key]);
                    minMaxAvg = endResult[device][key];
                    
                }
                    
            }
          //  console.log(avgX + "   " + avgY);
            if(count == 0)
                count = 1;
            avgX /= count;
            avgY /= count;
         //   console.log(avgX + "   " + avgY);
            result[device].push({x: avgX, y:avgY});  
            midStart = midEnd;
        }
        result[device].push(minMaxAvg); 
    }
    return result;
}

function get(msg) {
    var tmp = msg
    var params
    if(!tmp.DeviceID){
            params = {
            Username: tmp.Username  ,
            StartTime:  tmp.StartTime,
            EndTime:tmp.EndTime,
        }
    } else{
        params = {
            Username: tmp.Username  ,
            DeviceID: tmp.DeviceID,
            StartTime:  tmp.StartTime,
            EndTime:tmp.EndTime,
        }
        
    }
    console.log(params);
    patientManager.getDeviceMap({ Username: tmp.Username }).then(function (pat) { 
        dataManager.getGraphPoints(params).then(function(result){
            var endResult = {};
            var size = 1;
            if(!tmp.DeviceID)
                size = Object.keys(pat).length;
            console.log(size);
            for(var i= 0; i < size; i ++){
                var id = Object.keys(pat)[i];
                if(tmp.DeviceID)
                    id = tmp.DeviceID
                endResult[id] = [];
                for(var j = 0; j <  Object.keys(result).length - 1; j++){
                    endResult[id].push({x: result[Object.keys(result)[j]].x, y: result[Object.keys(result)[j]].y});
                }
                
                endResult[id].push({Min: result[Object.keys(result)[j]].Min, Max: result[Object.keys(result)[j]].Max, Avg: result[Object.keys(result)[j]].Avg});
            }
            endResult = compress(tmp.StartTime, tmp.EndTime, endResult);
            console.log(endResult);
            //channel(endResult);
        }).catch(function (e) {
            console.log(e);
            //logger.error('GraphRetievalError', e)
           // channel(false)
        }) 
    }).catch(function (e) {
        console.log(e);
        //logger.error('DeviceMapRetievalError', e)
        //channel(false)
    }) 
}
setTimeout(function() {
    var d = new Date();
    var n = d.getTime();
    console.log(n);
    start = n - 10000000000;
    end = n ;
    console.log(start);
    get({ Username: 'greg',StartTime: start ,EndTime:end,})
    
}, 0);
// DeviceID:"Body_temperature",
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
