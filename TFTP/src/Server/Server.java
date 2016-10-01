package Server;
import Utilities.PacketUtilities;
import Utilities.TFTPPacket;
import Utilities.TFTPRRQWRQPacket;
import Utilities.IO;
import java.io.*;
import java.net.*;


public class Server {
	//TODO:  Set port
	public final static int DEFAULT_PORT = 3001; //69
    private static final int SERVERPORT = DEFAULT_PORT; //FOR NOW
    private static final String defaultDir = System.getProperty("user.dir")+ "/storage/";
    private String publicFolder = defaultDir; // where all the file are stored
    byte[] buffer;
    DatagramSocket serverSocket;

    private boolean running;
    
    public Server() {
    	running = false;
    	buffer = new byte[PacketUtilities.DEFAULT_DATA_LENGTH];
        IO.print("FIle: "+ defaultDir);
    }
	
	

    public void run() {

        running = true;
        

        try {
            IO.print("S: Created Server Socket on port: "+ SERVERPORT+"...");
            serverSocket = new DatagramSocket(SERVERPORT);
        } catch (Exception e) {
            IO.print("S: Could not create server socket on port: "+ SERVERPORT);
            e.printStackTrace();
        }
        
        // Succesfully created Server socket... now waiting for connection
        while(running){
        	/* - Any connection begin with a read and write a file 
             * - also serves to request a connection
             * - If the server grants the request: open the connection
             *  
             */
        	DatagramPacket datagrampacket =  TFTPPacket.createDatagramForReceiving();;
        	try {
				// Accept incoming connections. 
        		serverSocket.receive(datagrampacket);
        		// receive() will block until a client connects to the server. 
                // If execution reaches this point, then it means that a client 
                // socket has been accepted.

            	IO.print("SERVER: Accepted connection.");
            	IO.print("SERVER: received"+new String(datagrampacket.getData(), 0, datagrampacket.getLength()));
            	
        	}catch(IOException e) {
        		IO.print("Exception encountered on accept.");
        		e.printStackTrace();
        	}
        	// For each client, we will start a service thread to 
        	// service the client requests. This is to demonstrate a 
            // Multi-Threaded server. Starting a thread also lets our 
            // Connection accept multiple connections simultaneously.
        	
        	try {
        		TFTPPacket packet = TFTPPacket.createFromDatagram(datagrampacket);
        		if (packet instanceof TFTPRRQWRQPacket) {
        			IO.print("Let create a new thread to handle the request!");
        			Connection clientConnection = new Connection((TFTPRRQWRQPacket)packet, datagrampacket.getAddress(), 
        														  datagrampacket.getPort(), this);
            		// Start a Service thread 
            		clientConnection.start();
        		}
        		
        	}catch(IllegalArgumentException e) {
        		e.printStackTrace(); // bad packet
        	}
        } //end while
        
        try { 
        	serverSocket.close(); 
            IO.print("Server Stopped"); 
        } catch(Exception e) { 
            IO.print("Problem stopping server socket"); 
            System.exit(-1); 
            }

      
    }
    
    public String getPublicFolder() {
		return publicFolder;
	}
	
	
	public static void main(String[] args) {
		//Create server and start it. 
		Server server = new Server();
		System.out.println("STARTING SERVER ON PORT: " + SERVERPORT);
		server.run();
	}

	
}
