package Server;


import java.io.IOException;
import java.net.*;
import java.util.*;

import Utilities.IO;



public class Connection extends Thread {
	
	//TODO:  Set port
		private int threadInstaceID;
		private DatagramSocket client;
	    private boolean running = false;
	    
	    
	    /**
	     * Constructor of the class
	     * @param messageListener listens for the messages
	     */
	    public Connection(DatagramSocket argClient, DatagramPacket packet) {
	        this.client = argClient;
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
	        this.threadInstaceID = threadInstaceID++; //@TODO need to fix this later
	        IO.print("Packet Processor, ID: " + this.threadInstaceID + " has started!");


	    }

}
