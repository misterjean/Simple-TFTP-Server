package Server;


import java.io.IOException;
import java.net.*;
import java.util.*;



public class Connection extends Thread {
	
	//TODO:  Set port
		private Thread t;
		private String threadName;
		private DatagramSocket client;
	    private static final int SERVERPORT = 3010; //FOR NOW
	    private boolean running = false;
	    private ArrayList<Connection> connections;
	    
	    
	    /**
	     * Constructor of the class
	     * @param messageListener listens for the messages
	     */
	    public Connection(DatagramSocket argClient, DatagramPacket packet,ArrayList<Connection> c) {
	        this.client = argClient;
	        this.connections = c;
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


	    }

}
