package Server;
import Utilities.PacketUtilities;
import Utilities.TFTPPacket;
import Utilities.TFTPRRQWRQPacket;
import Utilities.IO;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import static Utilities.PacketUtilities.DEFAULT_PORT;



public class Server {
	//TODO:  Set port
    static Scanner scanner = new Scanner(System.in);
    private static final int SERVERPORT = DEFAULT_PORT;
    private static String defaultDir;
    private String publicFolder = defaultDir; // where all the file are stored
    byte[] buffer;
    static DatagramSocket serverSocket;

    private boolean running;
    
    public Server() {
        defaultDir = System.getProperty("user.dir")+ "/storage/";
    	running = false;
    	buffer = new byte[PacketUtilities.DEFAULT_DATA_LENGTH];
    }

    public static void listFiles() {
        java.io.File[] files = new java.io.File(defaultDir).listFiles();
        if (files != null) {
            for (java.io.File f : files) IO.print(f.getName());
            IO.print(">>>>>>>>>>>>>END<<<<<<<<<<<<<");
        } else IO.print("No file is found");
    }

    public static void printCommands() {
        IO.print("<------------------------------------>");
        IO.print("help: show the help menu");
        IO.print("stop: stop the server");
        IO.print("ls: list all files in the working directory");
        IO.print("<------------------------------------>");
    }


    static synchronized void getCommands() {


        for (;;) {
            IO.print("Current working directory " + defaultDir);
            String cmdLine = scanner.nextLine().toLowerCase();
            String[] command = cmdLine.split("\\s+"); //This groups all white spaces as a delimiter.
            if (command.length == 0 || command[0].length() == 0) {
                continue;
            }

            if (command[0].equals("help")) {
                IO.print("Available commands:");
                printCommands();
            } else if (command[0].equals("stop")) {
                IO.print("Stopping server");
                serverSocket.close();
                System.exit(0);
                return;
            } else if (command[0].equals("ls")) {
                listFiles();
            } else if ((command[0].equals("cd"))
                    && command.length > 1 && command[1].length() > 0) {
                changeDir(command[1]);
            }else{
                IO.print("Invalid command. These are the available commands:");
                printCommands();
            }
        }
    }

    private static void changeDir(String f) {
        if (f.toCharArray()[f.length()-1] != '/')
        {
            f = f + '/';
        }

        File folder = new File (f);

        if (folder.isDirectory())
        {
            defaultDir = f;
        } else {
            IO.print(f + " is not a directory.");
        }
    }

    public void run() {

        running = true;

        try {
            IO.print("Server: Created Server Socket on port: "+ SERVERPORT+ "\n type 'help' for list of commands");
            serverSocket = new DatagramSocket(SERVERPORT);
        } catch (Exception e) {
            IO.print("Server: Could not create server socket on port: "+ SERVERPORT);
            e.printStackTrace();
        }
        
        // Succesfully created Server socket... now waiting for connection
        while(running){
        	/* - Any connection begin with a read and write a file 
             * - also serves to request a connection
             * - If the server grants the request: open the connection
             *  
             */
        	DatagramPacket datagrampacket =  TFTPPacket.createDatagramForReceiving();
            try {
				// Accept incoming connections. 
        		serverSocket.receive(datagrampacket);
        		// receive() will block until a client connects to the server. 
                // If execution reaches this point, then it means that a client 
                // socket has been accepted.


            	IO.print("SERVER: Accepted connection...");
            	IO.print("SERVER: received.."+new String(datagrampacket.getData(), 0, datagrampacket.getLength()));
            	
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
    
    public String getDefaultDir() {
		return defaultDir;
	}
	
	
	public static void main(String[] args) {
		//Create server and start it. 
		Server server = new Server();
        Thread commands = new Thread(new Commands(), "Command Prompt");
        commands.start();
        System.out.println("STARTING SERVER ON PORT: " + SERVERPORT);
		server.run();


	}

	
}

class Commands implements Runnable {

public void run(){ Server.getCommands(); }

}
