package Proxy;

//TFTPSim.java
//This class is the beginnings of an error simulator for a simple TFTP server 
//based on UDP/IP. The simulatorT receives a read or write packet from a client and
//passes it on to the server.  Upon receiving a response, it passes it on to the 
//client.
//One socket (23) is used to receive from the client, and another to send/receive
//from the server.  A new socket is used for each communication back to the client.   


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;

import Utilities.TFTPPacket;

public class TFTPSim {

	private enum ErrorCommands {
		NORMAL("normal");

		/**
		 * @param text
		 */
		private ErrorCommands(final String text) {
			this.text = text;
		}

		private final String text;

		@Override
		public String toString() {
			return text;
		}

	}


	protected InetAddress serverAddress;
	protected int serverRequestPort = 6900;
	protected int clientRequestPort = 2300;
	protected int threadCount = 0;
	protected boolean stopping = false;
	protected RequestReceiveThread requestReceive;

	private ErrorCommands errorCommand = ErrorCommands.NORMAL;

	/**
	 * Constructor
	 */
	public TFTPSim() {
		try {
			boolean isValid = false;
			while (!isValid) {
				isValid = true;
				System.out.print("Connect to:");
				Scanner scanner = new Scanner(System.in);
				String command = scanner.nextLine().toLowerCase();
				if (command.equalsIgnoreCase("localhost"))
					serverAddress = InetAddress.getLocalHost();
				else if (Character.isDigit(command.charAt(0)))
					serverAddress = InetAddress.getByName(command);
				else {
					System.out.println("localhost: for localhost\n"
							+ "192.168.0.1: for ip address");
					isValid = false;
				}
			}

			requestReceive = new RequestReceiveThread();
			requestReceive.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TFTPSim tftpSim = new TFTPSim();
		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.print("Command: ");
			String command = scanner.nextLine().toLowerCase();

			// Continue if blank line was passed
			if (command.length() == 0) {
				continue;
			}

			if (command.equals("help")) {
				printHelp();
				tftpSim.errorCommand = ErrorCommands.NORMAL;

			} else if (command.equals("stop")) {
				System.out
						.println("Stopping simulator (when current transfers finish)");
				tftpSim.stop();
				scanner.close();
			} else if (command
					.equalsIgnoreCase(ErrorCommands.NORMAL.toString())) {
				tftpSim.errorCommand = ErrorCommands.NORMAL;
			}else {
				tftpSim.errorCommand = ErrorCommands.NORMAL;
				printHelp();
			}
		}

	}

	private static void printHelp() {
		System.out.println("Available commands:");
		System.out.println("    help: prints this help menu");
		System.out.println("    stop: stop the error simulator (when current transfers finish)");
		System.out.println("    normal : normal mode ");
	}

	synchronized public void incrementThreadCount() {
		threadCount++;
	}

	synchronized public void decrementThreadCount() {
		threadCount--;
		if (threadCount <= 0) {
			notifyAll();
		}
	}

	synchronized public int getThreadCount() {
		return threadCount;
	}

	public void stop() {
		requestReceive.getSocket().close();

		// wait for threads to finish
		while (getThreadCount() > 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out
						.println("Stopping was interrupted. Failed to stop properly.");
				System.exit(1);
			}
		}
		System.out.println("Error simulator closed.");
		System.out.println();
		System.exit(0);
	}

	private class RequestReceiveThread extends Thread {
		private DatagramSocket socket;

		public RequestReceiveThread() {
			try {
				socket = new DatagramSocket(clientRequestPort);
				System.out.println("Socket created");

			} catch (SocketException e) {
				System.out.println("Count not bind to port: "
						+ clientRequestPort);
				System.exit(1);
			}
		}

		public void run() {
			try {
				incrementThreadCount();
				while (!socket.isClosed()) {
					DatagramPacket dp = TFTPPacket.createDatagramForReceiving();
					socket.receive(dp);
					new ForwardThread(dp).start();
				}
			} catch (IOException e) {
				// Probably just closing the thread down
			}

			decrementThreadCount();
		}

		public DatagramSocket getSocket() {
			return socket;
		}
	}

	private class ForwardThread extends Thread {
		private DatagramSocket socket;
		private int timeoutMs = 10000; // 10 second receive timeout
		private DatagramPacket requestPacket;
		private InetAddress clientAddress;
		private int clientPort, serverPort;

		ForwardThread(DatagramPacket requestPacket) {
			this.requestPacket = requestPacket;
		}

		public void run() {
			try {
				incrementThreadCount();

				socket = new DatagramSocket();
				socket.setSoTimeout(timeoutMs);
				clientAddress = requestPacket.getAddress();
				clientPort = requestPacket.getPort();

				// Send request to server
				System.out.println("Sending request to server ");

				DatagramPacket dp = new DatagramPacket( requestPacket.getData(),
													    requestPacket.getLength(), serverAddress,
						                                serverRequestPort);
				socket.send(dp);

				// Receive from server
				System.out.println("Receiving packet from server");
				dp = TFTPPacket.createDatagramForReceiving();
				socket.receive(dp);
				serverPort = dp.getPort();

				while (true) {
					// Forward to client
					System.out.println("Forwarding packet to client");
					dp = new DatagramPacket( dp.getData(), dp.getLength(),
											 clientAddress, clientPort);
					socket.send(dp);
					
					// Wait for response from client
					System.out.println("Waiting to get packet from client");
					dp = TFTPPacket.createDatagramForReceiving();
					socket.receive(dp);
					
					// Forward to server
					System.out.println("Forwarding packet to server");
					dp = new DatagramPacket( dp.getData(), dp.getLength(),
											 serverAddress, serverPort);
					socket.send(dp);
					

					// Receive from server
					System.out.println("Waiting to get packet from server");
					dp = TFTPPacket.createDatagramForReceiving();
					socket.receive(dp);
				}
			} catch (SocketTimeoutException e) {
				System.out
						.println("Socket timeout: closing thread. (Transfer may have simply finished)");
			} catch (IOException e) {
				System.out.println("Socket error: closing thread.");
			}

			decrementThreadCount();
		}
	}
}