package Client;

import Utilities.*;

import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.net.*;

import static Utilities.PacketUtilities.DEFAULT_PORT;
import static Utilities.PacketUtilities.PROXY_PORT;

public class Client {
	private static String defaultDir;
	InetAddress serverAddress;
	int serverRequestPort;
	private String fileName;
	private String filePath;
	private boolean mode = false;
	private static boolean verbose;
	private DatagramSocket sendReceiveSocket;
	private PacketUtilities packetUtilities;
	private TFTPTransferHandler transferHandler;

	public Client() {
        this.defaultDir = System.getProperty("user.dir") + "/clientStorage/";
		this.verbose = true;
		this.fileName = "";
		this.filePath = defaultDir+ fileName;
		this.serverRequestPort = DEFAULT_PORT;

		try {
			this.sendReceiveSocket = new DatagramSocket();

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public void setServerInfo(InetAddress serverAddress, int serverRequestPort) {
		this.serverAddress = serverAddress;
		this.serverRequestPort = serverRequestPort;
	}


	public TFTPTransferHandler getTFTPTransferHandler() throws TFTPAbortException {

		packetUtilities = new PacketUtilities(sendReceiveSocket);

		if (packetUtilities == null) {
			throw new TFTPAbortException("Server address not specified");
		}
		packetUtilities.setRemoteAddress(this.serverAddress);
		packetUtilities.setRequestPort(this.serverRequestPort);
		this.transferHandler = new TFTPTransferHandler(this.fileName, this.filePath, this.packetUtilities);
		return transferHandler;

	}

	public void setFilePath(TFTPRRQWRQPacket packet) {
		this.fileName = packet.getFilename();
		this.filePath = defaultDir + fileName;
	}

	public void getFile(String fileName) {
		this.filePath = defaultDir + fileName;
		this.fileName = fileName;
		try {
			transferHandler = getTFTPTransferHandler();
		} catch (TFTPAbortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		transferHandler.setFilePath(filePath);
		transferHandler.setFileName(fileName);
		this.transferHandler.receiveFileFromServer();
	}
	public void sendFile(String fileName) {
		this.fileName = fileName;
		this.filePath = defaultDir + fileName;
		try {
			transferHandler = getTFTPTransferHandler();
		} catch (TFTPAbortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		transferHandler.setFilePath(filePath);
		transferHandler.setFileName(fileName);
		IO.print("I Am about to send: " + fileName);

		IO.print("SEND: Current Port: "+sendReceiveSocket);
		this.transferHandler.sendFileToServer();

	}

	public static void listFiles() {
		java.io.File[] files = new java.io.File(defaultDir).listFiles();
		if (files != null) {
			for (java.io.File f : files) IO.print(f.getName());
			IO.print(">>>>>>>>>>>>>END<<<<<<<<<<<<<");
		} else IO.print("No file is found");
	}

	private static void changeDir(String f) {
        if (f.toCharArray()[f.length()-1] != '/')
        {
            f = f + '/';
        }
		File folder = new File (f);

		if (folder.isDirectory())
		{
			Client.defaultDir = f;
		} else {
			IO.print(f + " is not a directory.");
		}
	}

	public void printState(){
		if (mode == false) {
			IO.print("Mode is set to NORMAL");
		} else {
			IO.print("Mode is set to TESTING");
		}

		if (verbose == false){
			IO.print("Verbose is set to OFF");
		} else {
			IO.print("Verbose is set to ON");
		}

		IO.print("Current working directory " + defaultDir);
	}

	public static boolean getVerbose(){return verbose;}

	public static void start() {
		IO.print("<---------------------------------------------->");
		IO.print("help: show the help menu");
		IO.print("mode: toggle between normal and testing");
		IO.print("verbose: toggle verbose mode off or on");
		IO.print("read <FILENAME>: get the file from server");
		IO.print("write <FILENAME>: send the file to the server");
		IO.print("stop: stop the client");
		IO.print("ls: list all files in the working directory");
		IO.print("cd <DIRECTORYNAME>: change the working directory");
		IO.print("<---------------------------------------------->");
	}

	public void kill () {
		IO.print("CLIENT SHUTTING DOWN...");
		sendReceiveSocket.close();
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
			c.printState();
			IO.print("Client: ");
			String cmdLine = scanner.nextLine().toLowerCase();
			String[] command = cmdLine.split("\\s+"); //This groups all white spaces as a delimiter.

			// Continue if blank line was passed
			if (command.length == 0 || command[0].length() == 0) {
				continue;
			}

			if (command[0].equals("help")) {
				IO.print("Available commands:");
				start();
			} else if (command[0].equals("stop")) {
				IO.print("Stopping client");
				c.kill();
				scanner.close();
				return;
			} else if ((command[0].equals("read"))
					&& command.length > 1 && command[1].length() > 0) {
				c.getFile(command[1]);
			} else if ((command[0].equals("write"))
					&& command.length > 1 && command[1].length() > 0) {
				c.sendFile(command[1]);
			} else if (command[0].equals("mode")) {
				if (c.mode == true) {
					c.mode = false;
					c.serverRequestPort = DEFAULT_PORT;
				} else if (c.mode == false) {
					c.mode = true;
					c.serverRequestPort = PROXY_PORT;
				}
			} else if (command[0].equals("verbose")) {
				if (c.verbose == true) {
					c.verbose = false;
				} else if (c.verbose == false) {
					c.verbose = true;
				}
			} else if (command[0].equals("ls")) {
				listFiles();
            } else if ((command[0].equals("cd"))
                    && command.length > 1 && command[1].length() > 0) {

                changeDir(command[1]);
		}else{
					IO.print("Invalid command. These are the available commands:");
					start();
				}
			}
		}
}
