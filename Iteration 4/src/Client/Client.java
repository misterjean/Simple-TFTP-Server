package Client;

import Utilities.*;

import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.net.*;

public class Client {
	private static int DEFAULT_REQUEST_PORT = Config.SERVER_LISTENING_PORT;
	private static String defaultDir = System.getProperty("user.dir") + "/clientStorage/";;
	InetAddress serverAddress;
	int serverRequestPort;
	private boolean mode = false;
	private boolean verbose;

	public Client() {
		this.verbose = true;
	}


	public boolean getVerbose(){return verbose;}
	public boolean getMode() {return mode;}

	public void setServer(InetAddress serverAddress, int serverRequestPort) {
		this.serverAddress = serverAddress;
		this.serverRequestPort = serverRequestPort;
	}

	public void setServerRequestPort(int serverRequestPort) {
		this.serverRequestPort = serverRequestPort;
	}
	public void toggleVerbose() {
		this.verbose = !this.verbose;
	}
	public void toggleMode() {
		this.mode = !this.mode;
	}

	public String getConnectionString() {
		return "Currently connected to: "
				+ addressToString(serverAddress, serverRequestPort);
	}

	public String getPublicFolder() {
		return defaultDir;
	}

	public void stop() {
		System.out.println("Client is shutting down... goodbye!");
	}

	public TFTPConnection getConnection() throws TFTPAbortException {
		try {
			TFTPConnection conn = new TFTPConnection();

			if (serverAddress == null) {
				throw new TFTPAbortException("Server address not specified");
			}

			conn.setRemoteAddress(serverAddress);
			conn.setRequestPort(serverRequestPort);
			return conn;
		} catch (SocketException e) {
			String errMsg = "Failed to connect to " + serverAddress.toString()
					+ ":" + serverRequestPort;
			System.out.println(errMsg);
			throw new TFTPAbortException(errMsg);
		}
	}

	static private String addressToString(InetAddress addr, int port) {
		if (addr == null) {
			return "not connected";
		}

		return addr.toString() + ":" + port;
	}

	public void sendFileToServer( String fileName) {
		try {
			String filePath = getPublicFolder() + fileName;

			// Check that file exists
			File file = new File(filePath);
			if (!file.exists()) {
				System.out.println("Cannot find file: " + fileName);
				return;
			}

			// Check read permissions
			if (!file.canRead()) {
				System.out.println("Cannot read file: " + fileName);
				return;
			}

			// Open input stream
			FileInputStream fs = new FileInputStream(file);

			// Send request
			TFTPConnection conn = getConnection();
			conn.setVerbose(this.verbose);
			TFTPRRQWRQPacket reqPacket = TFTPPacket.createWriteRequestPacket(fileName,
					TFTPRRQWRQPacket.Mode.OCTET);
			conn.sendRequest(reqPacket);

			int blockNumber = 0;
			byte[] data = new byte[512];
			int bytesRead = 0;

			do {
				conn.receiveAck(blockNumber);
				blockNumber++;

				bytesRead = fs.read(data);

				// Special case when file size is multiple of 512 bytes
				if (bytesRead == -1) {
					bytesRead = 0;
					data = new byte[0];
				}

				conn.sendData(blockNumber, data, bytesRead);
			} while (bytesRead == TFTPDATAPacket.MAXFILEDATALENGTH);
			// Wait for final ACK packet
			conn.receiveAck(blockNumber);
			IO.print("Successfully sent file \'" + fileName + "\' to server");
			fs.close();

		} catch (TFTPAbortException e) {
			IO.print("Failed to send " + fileName + ": " + "\""+ e.getMessage() + "\"");
		} catch (IOException e) {
			IO.print("IOException: failed to send " + fileName + ": "+ "\"" + e.getMessage() + "\"");
		}
	}

	public void receiveFileFromServer(String fileName) {
		String filePath = getPublicFolder() + fileName;
		try {

			// Check write permissions
			File file = new File(filePath);
			if (file.exists() && !file.canWrite()) {
				System.out.println("Cannot overwrite file: " + fileName);
				return;
			}

			TFTPConnection conn = getConnection();
			conn.setVerbose(this.verbose);

			FileOutputStream fs = new FileOutputStream(filePath);

			TFTPRRQWRQPacket reqPacket = TFTPPacket.createReadRequestPacket(fileName,
					TFTPRRQWRQPacket.Mode.OCTET);

			conn.sendRequest(reqPacket);

			TFTPDATAPacket pk;

			int blockNumber = 1;

			do {

				pk = conn.receiveData(blockNumber);

				try {

					fs.write(pk.getFileData());
					fs.getFD().sync();
				} catch (SyncFailedException e) {
					file.delete();
					fs.close();
					conn.sendDiscFull("Failed to sync with disc, likely is full");
					return;
				}
				conn.sendAck(blockNumber);
				blockNumber++;
			} while (!pk.isLastDataPacket());
			IO.print("Done receiving file \'" + fileName + "\' from server");
			fs.close();

		} catch (TFTPAbortException e) {
			new File(filePath).delete();
			IO.print("Failed to get " + fileName + ": " + "\""
					+ e.getMessage() + "\"");
		} catch (IOException e) {
			new File(filePath).delete();
			IO.print("IOException: failed to get " + fileName + ": " + "\"" + e.getMessage() + "\"");
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
			Client.defaultDir = f;
		} else {
			IO.print(f + " is not a directory.");
		}
	}

	public static void listFiles() {
		java.io.File[] files = new java.io.File(defaultDir).listFiles();
		if (files != null) {
			for (java.io.File f : files) IO.print(f.getName());
			IO.print(">>>>>>>>>>>>>END<<<<<<<<<<<<<");
		} else IO.print("No files found");
	}

	public static void printCommandOptions() {
		IO.print("<---------------------------------------------->");
		IO.print("help: show the help menu");
		IO.print("mode: toggle between normal and testing");
		IO.print("verbose: toggle verbose mode off or on");
		IO.print("read <FILENAME>: get the file from server");
		IO.print("write <FILENAME>: send the file to the server");
		IO.print("stop: stop the client");
		IO.print("ls: list all files in the working directory");
		IO.print("cd <DIRECTORY>: change the working directory");
		IO.print("rm <FILENAME>: delete a given file in the directory");
		IO.print("connect ip|hostname: set the server IP or hostname (eg. connect 192.168.1.8)");
		IO.print("connect ip|hostname:portnumber: set the server IP or hostname and the port number (eg. connect 192.168.1.8:69");
		IO.print("show connection: show the current connection information: ip address and port number");
		IO.print("<---------------------------------------------->");

	}

	public static void main(String args[]) {
		Client c = new Client();
		Scanner scanner = new Scanner(System.in);

		try {
			c.setServer(InetAddress.getLocalHost(), DEFAULT_REQUEST_PORT );
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for(;;) {
			IO.print("Current working directory " + defaultDir);
			if (!c.getMode()) {
				IO.print("You are on Normal mode: Client <----> Server ");
			} else {
				IO.print("You are on Test mode: Client <----> TFTPSIM <-----> Server ");
			}
			if (c.getVerbose()) {
				IO.print("Verbose is turned on.");
			} else {
				IO.print("Verbose is turned off.");
			}

			System.out.print("Client: ");

			String cmdLine = scanner.nextLine().toLowerCase();
			String[] command = cmdLine.split("\\s+"); //This groups all white spaces as a delimiter.

			// Continue if blank line was passed
			if (command.length == 0 || command[0].length() == 0) {
				continue;
			}

			if (command[0].equals("help")) {
				IO.print("Available commands:");
				printCommandOptions();
			} else if (command[0].equals("stop")) {
				IO.print("Stopping client");
				c.stop();
				scanner.close();
				return;
			} else if (command[0].equals("read")
					&& command.length > 1 && command[1].length() > 0) {
				c.receiveFileFromServer(command[1]);
			} else if ((command[0].equals("write"))
					&& command.length > 1 && command[1].length() > 0) {
				c.sendFileToServer(command[1]);

			} else if (command[0].equals("ls")) {
				listFiles();
			} else if (command[0].equals("mode")) {
				if (c.getMode()) {
					c.toggleMode();
					c.setServerRequestPort(Config.SERVER_LISTENING_PORT);
				} else {
					c.toggleMode();
					c.setServerRequestPort(Config.PROXY_LISTENING_PORT);
				}
			} else if (command[0].equals("verbose")) {
				if (c.getVerbose()) {
					c.toggleVerbose();
				} else {
					c.toggleVerbose();
				}
			}else if ((command[0].equals("connect")) && command.length > 1
					&& command[1].length() > 0) {
				try {
					String connectComponents[] = command[1].split(":");
					int serverPort = DEFAULT_REQUEST_PORT;
					if (connectComponents.length >= 2) {
						try {
							serverPort = Integer.parseInt(connectComponents[1]);
							if (serverPort < 0) {
								System.out.println("Invalid port number. Port number cannot be negative. Failed to connect.");
								continue;
							}
						} catch (NumberFormatException e) {
							System.out.println("Invalid port number. Port number must be an integer. Failed to connect.");
							continue;
						}
					}
					c.setServer(InetAddress.getByName(connectComponents[0]),
							serverPort);
				} catch (UnknownHostException e) {
					System.out.println("Failed to connect to " + command[1]);
				}
			} else if (command[0].equals("show") && command.length > 1) {
				if (command[1].equals("connection")) {
					System.out.println(c.getConnectionString());
				} else {
					System.out.println("Invalid command. These are the available commands:");
					printCommandOptions();
				}
			} else if ((command[0].equals("cd"))
				&& command.length > 1 && command[1].length() > 0) {
				changeDir(command[1]);

			} else if (command[0].equals("rm")
				&& command.length > 1 && command[1].length() > 0) {

				File f = new File(defaultDir + command[1]);

				if (f.exists()) {
					f.delete();
				} else {
					IO.print("Cannot find file " + command[1]);
				}
		}else {
				System.out.println("Invalid command. These are the available commands:");
				printCommandOptions();
			}
		}
	}
}
