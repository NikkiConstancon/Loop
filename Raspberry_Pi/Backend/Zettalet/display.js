module.exports = function(server){

		console.log('Outside');
		
	var query = server.where({type : 'state_machine'});
	server.observe([query],function(object){
		console.log('Inside');
		console.log(object);
	});

}