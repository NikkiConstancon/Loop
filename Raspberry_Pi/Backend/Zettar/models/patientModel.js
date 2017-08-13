module.exports = {
    fields:{
    	Username: "text",			
        PatientPassword: {
    		type: "text",
    		rule: {
    			required: true
    		}
    	},
    	AccessPassword: {
    		type: "text",
    		rule: {
    			required: true
    		}
    	},
        SubscriberList: {
            type: "list",
            typeDef: "<text>"
        }, 
        PatientEmail: {
        	type: "text",
        	rule: {
        		validator : function(email){ 
				    var re = /\S+@\S+\.\S+/;
    				return re.test(email); 
                },
                message: 'Email is in the incorrect form',			
			    required: true 
            }
        },	
        Address : "text", 
        Age: {
        	type: "int",
        	rule: {
            	validator : function(value){ return value > 0; },
            	message   : 'Age must be greater than 0'
        	}
    	},	
        Weight: {
        	type: "float",
        	rule: {
            	validator : function(value){ return value > 0; },
            	message   : 'weight must be greater than 0'
        	}
    	},
        Height: {
        	type: "float",
        	rule: {
            	validator : function(value){ return value > 0; },
            	message   : 'Hieght must be greater than 0'
        	}
    	},
        Reason: {
        	type: "text",
        	rule: {
            	validator: function(value){ 
				    var val = value.toLowerCase();
				    if(val == 'age' || val == 'illness' || val == 'disability' || val == 'accident' || val == 'other')
					   return true;
				    return false; 
                },
            	message: 'Reason is not included in the set of reasons.'
        	}
    	}	
    },
    key:["Username"]		
}
