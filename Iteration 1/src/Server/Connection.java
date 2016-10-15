package Server;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SyncFailedException;
import java.net.*;
import java.util.*;

import Client.Client;
import Utilities.IO;
import Utilities.PacketUtilities;
import Utilities.TFTPAbortException;
import Utilities.TFTPDATAPacket;
import Utilities.TFTPPacket;
import Utilities.TFTPRRQWRQPacket;
import Utilities.TFTPTransferHandler;



public class Connection extends Thread {
	
	//TODO:  Set port
		private int threadInstaceID;
		private PacketUtilities packetUtilities;
		private DatagramSocket currentConnection;
		private int port;
		private String fileName;
		private String filePath;
		private InetAddress address;
		private boolean isReadRequest;
		private Server server;
		private TFTPTransferHandler tftpTransferHandler;
	    byte[] buffer = new byte[PacketUtilities.DEFAULT_DATA_LENGTH];
	    
	    
	    /**
	     * Constructor of the class
	     * @param messageListener listens for the messages
	     */
	    public Connection(TFTPRRQWRQPacket packet, InetAddress address, int port, Server server) {
	    	this.port = port;
	    	IO.print("port"+ port);
			this.address = address;
			this.server = server;
			this.fileName = packet.getFilename();
			this.filePath = this.server.getPublicFolder() + fileName;
			this.isReadRequest = packet.isReadRequest();

        	//new socket created with random port for thread
	        try {
				this.currentConnection = new DatagramSocket();
		    	packetUtilities = new PacketUtilities(currentConnection);
		    	packetUtilities.setRemoteAddress(this.address);
		    	packetUtilities.setRemoteTid(this.port);
		    	this.tftpTransferHandler = new TFTPTransferHandler(this.fileName, this.filePath, this.packetUtilities);
		    	
		    	
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    

	    /**
	     * Method to send the messages from server to client
	     * @param message the message sent by the server
	     */
	    public void sendDatagramPacket(DatagramPacket packet) {
	    	 
	    
	    }
	    
	  

	    @Override
	    public void run() {
	        super.run();
	        //this.threadInstaceID = threadInstaceID++; //@TODO need to fix this later
			if (Client.getVerbose() == true) {
				IO.print("Packet Processor, ID: " + this.threadInstaceID + " has started!");
			}
			// while we receive data packets that are 516 in size (break inside while)
	        
	        if (isReadRequest) {
	        	IO.print("It's a read request");
	        	this.tftpTransferHandler.sendFileToClient();
	        	
	        }else { //it's a write request
	        	IO.print("Its a write request");
	        	this.tftpTransferHandler.receiveFileFromClient();
	        }

	    }
}
