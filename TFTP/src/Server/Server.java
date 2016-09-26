package Server;
import java.util.ArrayList;
import java.util.Random;
import java.io.*; 
import java.net.*;
import java.util.*;


public class Server {
	//TODO:  Set port
	public final static int DEFAULT_PORT = 69;
    private static final int SERVERPORT = DEFAULT_PORT; //FOR NOW
    byte[] buffer = new byte[512];

    private boolean running = false;
	
	

    public void run() {

        running = true;
        

        try {
            System.out.println("S: Connecting...");

            DatagramSocket serverSocket = new DatagramSocket(SERVERPORT);
            ArrayList<Connection> connections = new ArrayList<>();

            while(running){
            
                DatagramPacket packet =  new DatagramPacket(buffer, buffer.length );
                serverSocket.receive(packet);
                System.out.println("SERVER: Accepted connection.");
                System.out.println("SERVER: received"+new String(packet.getData(), 0, packet.getLength()));
               
                //new socket created with random port for thread
                DatagramSocket threadSocket = new DatagramSocket();
                
                Connection clientConnection = new Connection(threadSocket, packet, connections);
                connections.add(clientConnection);
                clientConnection.start();
            } //end while
            

        } catch (Exception e) {
            System.out.println("S: Error");
            e.printStackTrace();
        }

      
    }
	
	
	public static void main(String[] args) {
		//Create server and start it. 
		Server server = new Server();
		System.out.println("STARTING SERVER ON PORT: " + SERVERPORT);
		server.run();
	}

	
}
