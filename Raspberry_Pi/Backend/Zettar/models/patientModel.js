module.exports = {
    fields:{
    	Username	: text,
    							//what about name and surname?
        Password    : "text",	//encryption? type? perhaps call it PatientPassword?
        SubscriberPassword : "text", //encryption? type? perhaps call it AccessPassword?
        SubscriberList     : "int", //type?
        Email     : "text",	//perhaps call it PatientEmail? is there an email type?
        Address     : "text", //what format will they be sending it to us?
        Age     : "int",	
        Weight     : "int",
        Height     : "int",
        Reason     : "text"	//text I'm guessing?
    },
    key:["Username"]		//note it should be same as Server name on Zettalet right?
}
