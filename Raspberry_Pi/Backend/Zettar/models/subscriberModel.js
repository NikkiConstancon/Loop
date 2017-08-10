module.exports = {
    fields:{
    	Email : "text",		//email type?
    		//name/surname?
        Password    : "text",	//encryption? perhaps call it SubscriberPassword?
        Relation : "text",
        PatientList     : "int" //dataype?
    },
    key:["Email"]	//no username for Subscriber?
}
