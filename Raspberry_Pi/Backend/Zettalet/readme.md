#To install all dependencies:


> npm install


#To run the server:


> node zettalet


#If you add more node modules:


> npm install --save packageName


#Raspberry Pi Setup

1. Ensure there is a internet connection

2. Disable password on startup

3. Automatically login as user

4. Write script to run at start up
	- runZettalet.js --> node absolutePathToFolder/zettalet 
	- got to etc/init
	- open terminal
	- sudo touch runZettalet.conf
	- sudo textEditorName runZettalet.conf
	- add to file: sudo absolutePathToFolder/runZettalet.sh

5. Make sure the link() in zettalet.js points to 197.242.150.255:3009

