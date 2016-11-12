# SYSC3303 - Fall 2016
# Collaborative project - Iteration 2
#### October 15th 2016

### Setup Instructions
1. Import the project. File -> Import -> Existing Projects Into Workspace OR create a new JAVA project and copy the three folders in src (excluding .idea) into the src folder of your new project. 
2. Expand src\core\.
3. Run the Server Class. Right click the file in the navigation bar, then 'Run As' and then 'Java Application' 
4. Run the TFTPSim Class. 
5. Run the Client Class.
6. On the client, server or proxy, type 'help' for a list of commands (including read, write and change directory and more)

Files of varying sizes to test file transfers with have been stored in both "/storage" and "/clientStorage" folders.
	
	Note: If the Server and Client are running in the same directory (Eclipse default), this will cause an obvious error. To prevent this,
	you may optionally set the Server or Client's default directory to another location via editing the run configurations for the class,
	(Right Click -> Run As... -> Run Configurations -> Arguments -> Working Directory -> Other -> Specify the directory of your choice). Of course you can always use the absolute path when reading or writing files and use different directories there. We recommend you use the M drive as your directory (when testing on the lab computers) for writing since it is the only one we are aware of where you have write permission.
	

# Developers
####Team Eight

###### Responsible for the Client and documentation
Jean-Elie Jean-Gilles	(100856860)

###### Responsible for the Proxy and Utilities
Yue Zhang 				(100980408)

###### Responsible for the error handling
Emmanuel Guelor 		(100884107)

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

