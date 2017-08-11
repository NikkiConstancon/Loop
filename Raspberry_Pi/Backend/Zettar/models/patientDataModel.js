module.exports = {
    fields:{
        PatientUsername    : "text",
        DeviceID : "int",		//what exactly is device ID?
        TimeStamp     : "int",	//date? datetime? interval? what formats are there? which are apporpiate
        Value : "text"			//"can be a double or a set of information" so text? JSON string?
    },
    key:["patientName", "DeviceID", "TimeStamp"] //is this how you do multiple keys?
}
