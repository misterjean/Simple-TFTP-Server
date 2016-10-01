package Client;

import Utilities.*;

import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.net.*;

import static Utilities.PacketUtilities.DEFAULT_PORT;

public class Client {
    private static final String defaultDir = System.getProperty("user.dir") + "/clientStorage/";
    InetAddress serverAddress;
	int serverRequestPort;
	private String fileName;
	private String filePath;
    private DatagramSocket sendReceiveSocket;
    private PacketUtilities packetUtilities;
    private TFTPTransferHandler transferHandler;

    public Client() {
    	this.fileName = "";
		this.filePath = defaultDir+ fileName;
		
		try {
			this.sendReceiveSocket = new DatagramSocket();
	    	packetUtilities = new PacketUtilities(sendReceiveSocket);
	    	packetUtilities.setRemoteAddress(this.serverAddress);
	    	packetUtilities.setRemoteTid(this.serverRequestPort);
	    	this.transferHandler = new TFTPTransferHandler(this.fileName, this.filePath, this.packetUtilities);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void setServerInfo(InetAddress serverAddress, int serverRequestPort) {
		this.serverAddress = serverAddress;
		this.serverRequestPort = serverRequestPort;
		packetUtilities.setRemoteAddress(this.serverAddress);
    	packetUtilities.setRemoteTid(this.serverRequestPort);
	}
    
    public void setFilePath(TFTPRRQWRQPacket packet) {
    	this.fileName = packet.getFilename();
    	this.filePath = defaultDir + fileName;
    }
    
    public void getFile(String fileName) {
    	this.filePath = defaultDir + fileName;
    	this.fileName = fileName;
    	transferHandler.setFilePath(filePath);
    	transferHandler.setFileName(fileName);
		IO.print("I Am about to get: " + fileName);

    	this.transferHandler.receiveFileFromServer();
    }
    public void sendFile(String fileName) {
    	this.fileName = fileName;
    	this.filePath = defaultDir + fileName;
    	transferHandler.setFilePath(filePath);
    	transferHandler.setFileName(fileName);
		IO.print("I Am about to send: " + fileName);

    	this.transferHandler.sendFileToServer();
    	
    }

    public static void start() {
        IO.print("<------------------------------->");
        IO.print("help: show the help menu");
        IO.print("kill: kill the client");
        IO.print("get: get the file from server");
        IO.print("send: send the file to the server");
		IO.print("<------------------------------->");
    }
    
    public void kill () {
    	IO.print("Nooo! I am dying...");
    }


    public static void main(String args[]) {
        Client c = new Client();
    	Scanner scanner = new Scanner(System.in);
    	
        try {
			c.setServerInfo(InetAddress.getLocalHost(), DEFAULT_PORT );
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        for(;;) {
        	
        	IO.print("Client: ");
			String cmdLine = scanner.nextLine().toLowerCase();
			String[] command = cmdLine.split("\\s+"); //This groups all white spaces as a delimiter.

			// Continue if blank line was passed
			if (command.length == 0 || command[0].length() == 0) {
				continue;
			}
			
			if (command[0].equals("help")) {
				System.out.println("Available commands:");
				start();
			} else if (command[0].equals("kill")) {
				System.out.println("Stopping client");
				c.kill();
				scanner.close();
				return;
			} else if ((command[0].equals("get"))
					&& command.length > 1 && command[1].length() > 0) {
				c.getFile(command[1]);
			} else if ((command[0].equals("send"))
					&& command.length > 1 && command[1].length() > 0) {
				c.sendFile(command[1]);
			} else {
				IO.print("Invalid command. These are the available commands:");
				start();
			}
        }
    }
}
