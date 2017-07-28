

At the moment, run both zettar and zettalet in seperate terminals.

Zettar will be deployed on the cloud and Zettlet will be deployed on each Raspberry Pi


#Where to find what:


1. Go Zettar/node_modules/zetta/lib

2. In http_server.js you will find the code where the connection to the raspberry pi's is established. Look at ZettaHttpServer.prototype.setupPeerSocket

3. In pubsub_service.js you will find where the streamed value is published. Look at PubSub.prototype.publish


#Instructions for it to work:

1. Run the zettar and as many zettalets as you want

2. Go to: http://browser.zettajs.io/#/overview?url=http:%2F%2F127.0.0.1:3009

3. If it is running on the cloud server then go to:  http://browser.zettajs.io/#/overview?url=http:%2F%2F197.242.150.255:3009


This webpage essentially subscribes to the stream (This will be replaced with the andriod app at some point) then on the Zettar server the details are published.

#TODO:


Extract the relevant informtion from the stream and then asynchronously store it in the database.


#Server Information:

There is a rudementary Zettar server running on 197.242.150.255:3009

