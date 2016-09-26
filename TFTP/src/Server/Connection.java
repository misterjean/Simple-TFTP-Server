package Server;


import java.io.IOException;
import java.net.*;
import java.util.*;

import Utilities.IO;
import Utilities.PacketUtilities;



public class Connection extends Thread {
	
	//TODO:  Set port
		private int threadInstaceID;
		private DatagramSocket client; 
	    byte[] buffer = new byte[PacketUtilities.DEFAULT_DATA_LENGTH];
	    private boolean running = false;
	    
	    
	    /**
	     * Constructor of the class
	     * @param messageListener listens for the messages
	     */
	    public Connection(DatagramPacket packet) {
        	//new socket created with random port for thread
	        try {
				this.client = new DatagramSocket();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    

	    /**
	     * Method to send the messages from server to client
	     * @param message the message sent by the server
	     * @throws IOException 
	     * @throws JSONException 
	     */
	    public void sendDatagramPacket(DatagramPacket packet) {
	    	 
	    
	    }
	    
	  

	    @Override
	    public void run() {
	        super.run();
	        //this.threadInstaceID = threadInstaceID++; //@TODO need to fix this later
	        IO.print("Packet Processor, ID: " + this.threadInstaceID + " has started!");


	    }

}
