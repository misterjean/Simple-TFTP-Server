package Server;
import Utilities.*;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import static Utilities.PacketUtilities.DEFAULT_PORT;
import Utilities.TFTPErrorPacket.ErrorType;
import Utilities.PacketUtilities;


public class Server {
	//TODO:  Set port
    static Scanner scanner = new Scanner(System.in);
    private static final int SERVERPORT = DEFAULT_PORT;
    private static String defaultDir;
    private String publicFolder = defaultDir; // where all the file are stored
    byte[] buffer;
    static DatagramSocket serverSocket;
    private static boolean verbose;

    private boolean running;
    
    public Server() {
        this.verbose = true;
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
        IO.print("verbose: toggle verbose mode");
        IO.print("<------------------------------------>");
    }

    public static boolean getVerbose(){return verbose;}
    public void toggleVerbose() {}


     static synchronized void getCommands() {


        for (;;) {
            IO.print("Current working directory " + defaultDir);

            if (verbose == false){
                IO.print("Verbose is set to OFF");
            } else {
                IO.print("Verbose is set to ON");
            }

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
            } else if (command[0].equals("verbose")){
                verbose = !verbose;
            } else{
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
                   /* if (!verbose) {
                        clientConnection.toggleVerbose();
                    } else if (verbose){
                        clientConnection.toggleVerbose();
                    }*/
            		clientConnection.start();

        		}else {
                    // We received a valid packet but not a request
                    // Ignore error packets, otherwise send error
                    if (!(packet instanceof TFTPErrorPacket)) {
                        // Protocol ambiguity: could send either
                        // an illegal op or unknown TID
                        // The following implementation opted for
                        // sending an illegal op
                        try {
                            DatagramSocket errorSocket = new DatagramSocket();
                            String errMsg = "Received the wrong kind of packet on request listener.";
                            TFTPErrorPacket errorPacket = TFTPPacket
                                    .createErrorPacket(
                                            ErrorType.ILLEGAL_OPERATION, errMsg);
                            datagrampacket = errorPacket.generateDatagram(datagrampacket.getAddress(),
                                    datagrampacket.getPort());
                            try {
                                errorSocket.send(datagrampacket);
                                errorSocket.close();
                            }catch (IOException e) {
                                e.printStackTrace();
                            }

                            IO.print("Sending illegal operation error packet with message: "
                                    + errMsg);
                        }catch(SocketException e) {
                            e.printStackTrace();
                        }
                    }
                }
        		
        	}catch(IllegalArgumentException e) {
                // We got an invalid packet
                // Open new socket and send error packet response
                try {
                    DatagramSocket errorSocket = new DatagramSocket();
                    IO.print("Server received invalid request packet");
                    TFTPErrorPacket errorPacket = TFTPPacket.createErrorPacket(
                            ErrorType.ILLEGAL_OPERATION, e.getMessage());
                    datagrampacket = errorPacket.generateDatagram(datagrampacket.getAddress(),
                            datagrampacket.getPort());
                    try {
                        errorSocket.send(datagrampacket);
                        errorSocket.close();
                    }catch (IOException io){
                        io.printStackTrace();
                    }
                }catch (SocketException se) {
                    se.printStackTrace();
                }
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
