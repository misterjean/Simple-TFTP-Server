package Server;
import Utilities.PacketUtilities;
import Utilities.IO;
import java.util.ArrayList;
import java.util.Random;
import java.io.*; 
import java.net.*;
import java.util.*;


public class Server {
	//TODO:  Set port
	public final static int DEFAULT_PORT = 69;
    private static final int SERVERPORT = DEFAULT_PORT; //FOR NOW
    byte[] buffer = new byte[PacketUtilities.DEFAULT_DATA_LENGTH];
    DatagramSocket serverSocket;

    private boolean running = false;
	
	

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
        	DatagramPacket packet =  new DatagramPacket(buffer, buffer.length );
        	try {
				// Accept incoming connections. 
        		serverSocket.receive(packet);
        		// receive() will block until a client connects to the server. 
                // If execution reaches this point, then it means that a client 
                // socket has been accepted.

            	IO.print("SERVER: Accepted connection.");
            	IO.print("SERVER: received"+new String(packet.getData(), 0, packet.getLength()));
            	
        	}catch(IOException e) {
        		IO.print("Exception encountered on accept.");
        		e.printStackTrace();
        	}
        	
        	//new socket created with random port for thread
        	DatagramSocket threadSocket;
			try {
                // For each client, we will start a service thread to 
                // service the client requests. This is to demonstrate a 
                // Multi-Threaded server. Starting a thread also lets our 
                // Connection accept multiple connections simultaneously. 
				threadSocket = new DatagramSocket();
	        	Connection clientConnection = new Connection(threadSocket, packet);
	        	
                // Start a Service thread 
	        	clientConnection.start();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	
	
	public static void main(String[] args) {
		//Create server and start it. 
		Server server = new Server();
		System.out.println("STARTING SERVER ON PORT: " + SERVERPORT);
		server.run();
	}

	
}
