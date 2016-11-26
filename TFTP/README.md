# SYSC3303 - Fall 2016
# Collaborative project - Iteration 4
#### November 26th 2016

### Setup Instructions
1. Import the project. File -> Import -> Existing Projects Into Workspace OR create a new JAVA project and copy the three folders in src (excluding .idea) into the src folder of your new project. 
2. Expand src\.
3. Run the Server Class. Right click the file in the navigation bar, then 'Run As' and then 'Java Application' 
4. Run the TFTPSim Class. 
5. Run the Client Class.
6. On the client, server or proxy, type 'help' for a list of commands (including read, write and change directory and more)

####Testing file I/O errors
- Access Violation: By simply calling chmod 555 on your working directory, you should be able to test access violations.
- Disk Full: Could be tested by changing working directory to a USB stick that has reached its capacity.
- File Not Found: Simply request to read or write a file that does not exist.
- File Already Exists: Simply try to write a file that already exists in the working directory in either the client or server.

####Testing the Error Simulator (TFTPSim)
	The Proxy can now simulate multiple different types of packet errors such as packet loss, packet delay and packet duplication 
	on read request packets, write request packets, data packets and acknowledgement packets. To do so, simply type "localhost" in the 
	command line after launching the proxy to connect on device, then type help to see a list of commands and choose which type of error to generate.

- e.g. (duplicating a packet):
```sh
Connect to: localhost
localhost: for localhost
192.168.0.1: for ip address
Connect to:localhost
Command: help
Available commands:
    help: prints this help menu
    stop: stop the error simulator (when current transfers finish)
normal : normal mode 
mode 0 : change packet opcode 
mode 1: Remove the byte '0' after the file name
mode 2: Remove the byte '0' after the mode
mode 3: Modify the string mode
mode 4: Append more data to the packet
mode 5: Shrink the packet
mode 6: Remove File name from the packet
mode 7: Change the port number - Invalid TID
mode 8: Lose a packet
mode 9: Delay a packet
mode 10: Duplicate a packet
mode 18: Change Block Number
Command: mode 10
Choose Packet Type:  data packet(mode 15) or ack Packet(mode 16):mode 16
Choose the Block Number: 1
```

####Files of varying sizes to test file transfers with have been stored in both "/storage" and "/clientStorage" folders.
	
	Note: The server and client should have their own working directories on launch but in the event that the Server and Client are running in the same directory 
	you may optionally set the Server or Client's default directory to another location by calling the in program cd command. Simply type cd <FOLDERPATH> and the
	working directory should be changed.
	

# Developers
####Team Eight

###### Responsible for the Client/Server maintnance and documentation
Jean-Elie Jean-Gilles	(100856860)

###### Responsible for the Error Simulator
Yue Zhang 				(100980408)

###### Responsible for the Server/Client error packet handling
Guelor Emmanuel  		(100884107)

###### Responsible for the timing Diagrams 
Zakaria Hassan			(100860461)



# File Hierarchy
## .
##### This is the root of the project directory. It contains files that are vital to the project being available for immediate setup in Eclipse. It also contains files such as this README, the project License, build scripts, and respective sub-directories.
### README.md
##### The current file, containing information about the entire project.
### /src
##### Contains the source code for the project. Source is broken into several packages. core contains the runnable source code, i.e, Client, Server, and Proxy. Utilities contains a number of classes to help with tasks that are used through out the entire program, numerous times as well as TFTP handler files. 
### /documentation
##### Contains all diagrams for the project.
### /storage
##### This is the default folder for the Server
### /clientStorage
##### This is the default folder for the Client

