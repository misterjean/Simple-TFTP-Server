package Utilities;

import Client.Client;
import Server.Server;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Yue on 2016-09-19.
 * this class contains every utility functions related to packets
 */
public class PacketUtilities {

    /**
     * the default length of the buffer inside packets
     */
    public static final int DEFAULT_DATA_LENGTH = 516;


	public static final int DEFAULT_PORT = 6900; //Server PORT 69
	public static final int PROXY_PORT = 2300; //Proxy port 23
	

    /**
     * an empty buffer for packets
     */
    public static byte[] rawData = new byte[DEFAULT_DATA_LENGTH];
    
    private DatagramSocket socket;
	private InetAddress remoteAddress;

	private int requestPort = 9000; //default request port over 9000, this is being set before being used by the setMethod!
	private int remoteTid = -1;   // default TID being set by the setmethod!
	private DatagramPacket rcvDatagram = TFTPPacket.createDatagramForReceiving();
	private DatagramPacket sendDatagram;
	private DatagramPacket resendDatagram;
	private int maxResendAttempts = 4;
	private int timeoutTime = 2000;




	public PacketUtilities(DatagramSocket currentConnection){
		this.socket = currentConnection;
	}

    /**
     * to create an packet with empty buffer
     * @return a packet with empty buffer
     */
    public static DatagramPacket createEmptyPacket(){
        DatagramPacket packet = new DatagramPacket(rawData, DEFAULT_DATA_LENGTH);
        return packet;
    }

    /**
     *to get the ID, which is the second byte in the buffer, from a packet
     * @param packet the target packet
     * @return the ID
     */
    public static int getPacketID(DatagramPacket packet){
        return packet.getData()[1];
    }

	public static int getBlockNum(DatagramPacket packet) {
		byte[] blockID = {packet.getData()[2], packet.getData()[3]};
		ByteBuffer wrapped = ByteBuffer.wrap(blockID);
		Short num = wrapped.getShort();
		return ((int) num);
	}

    public static String getPacketType(DatagramPacket packet){
        if( getPacketID(packet)==1 ) return "Read Request";
        else if ( getPacketID(packet)==2 ) return "Write Request";
        else if ( getPacketID(packet)==3 ) return "Data";
        else if ( getPacketID(packet)==4 ) return "ACK";
        else return "Expected Type! This should NOT happen!";
    }

    /**
     * This method returns the packet name of a packet
     * @param p target packet
     * @return packet name of the target packet
     */
    public static String getPacketName(DatagramPacket p){
        if( isRRQPacket(p) ) return "RRQ Packet";
        else if( isWRQPacket(p) ) return "WRQ Packet";
        else if( isDATAPacket(p) ) return "DATA Packet";
        else if( isACKPacket(p) ) return "ACK Packet";
        else return "ERROR Packet";
    }

	/**
	 * Another version of getPacketName()
	 * @param opcode
	 * @return packet name
     */
	public static String getPacketName(int opcode){
		if( opcode == 1 ) return "RRQ Packet";
		else if( opcode == 2 ) return "WRQ Packet";
		else if( opcode == 3 ) return "DATA Packet";
		else if( opcode == 4 ) return "ACK Packet";
		else return "ERROR Packet";
	}

    /**
     * This method checks if a packet is a RRQ packet
     * @param p the target packet
     * @return true if the target packet is a RRQ packet, false otherwise
     */
    public static boolean isRRQPacket(DatagramPacket p){
        return getPacketID(p) == 1;
    }

    /**
     * This method checks if a packet is a WRQ packet
     * @param p the target packet
     * @return true if the target packet is a WRQ packet, false otherwise
     */
    public static boolean isWRQPacket(DatagramPacket p){
        return getPacketID(p) == 2;
    }

    /**
     * This method checks if a packet is a ACK packet
     * @param p the target packet
     * @return true if the target packet is a ACK packet, false otherwise
     */
    public static boolean isACKPacket(DatagramPacket p){
        return getPacketID(p) == 4;
    }

    /**
     * This method checks if a packet is a DATA packet
     * @param p the target packet
     * @return true if the target packet is a DATA packet, false otherwise
     */
    public static boolean isDATAPacket(DatagramPacket p){
        return getPacketID(p) == 3;
    }


    /**
     * to determine whether the target packet is the last packet or not
     * the last packet will contain less than 516 bytes in its buffer
     * @param packet the target packet
     * @return true if the target pacekt is the last packet, false otherwise
     */
    public static boolean isLastPacket(DatagramPacket packet){
        return packet.getLength() < DEFAULT_DATA_LENGTH;
    }

    /**
     * the function used to send packets and print out the packets
     *
     * @param packet packet that is being sent
     */
    public static void send(DatagramPacket packet, DatagramSocket socket) {

        if( socket.isClosed() ){
            System.out.println("Socket is closed, unable to send packets");
        }

        try {

            socket.send(packet);
			if (Client.getVerbose() == true) {
				printPacketDetails(2, packet);
			}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendRequest(TFTPRRQWRQPacket packet) throws IOException {
    	sendDatagram = packet.generateDatagram(remoteAddress, requestPort);
    	resendDatagram =  packet.generateDatagram(remoteAddress, requestPort);
		send(sendDatagram, socket);
		
	}

    
    private void send(TFTPPacket packet) throws IOException {
		DatagramPacket dp = packet.generateDatagram(remoteAddress, remoteTid);
		if (Server.getVerbose()) {
			printPacketDetails(1, dp);
		}
		send(packet, false);
	}

	private void send(TFTPPacket packet, boolean cacheForResend)
			throws IOException {
		DatagramPacket dp = packet.generateDatagram(remoteAddress, remoteTid);

		if (Server.getVerbose())
			printPacketDetails(2, dp);

		if (cacheForResend) {
			resendDatagram = dp;
		} else {
			resendDatagram = null;
		}
		send(dp, socket);
	}
    
    public void sendAck(int blockNumber) throws TFTPAbortException {
		try {
			send(TFTPPacket.createACKPAcket(blockNumber));
		} catch (Exception e) {
			throw new TFTPAbortException(e.getMessage());
		}
	}
	private void echoAck(int blockNumber) throws IOException {
		send(TFTPPacket.createACKPAcket(blockNumber));
		IO.print("sent: ack #" + blockNumber + " in response to duplicate data");
	}

	private void resendLastPacket() throws TFTPAbortException {
		if (resendDatagram == null) {
			return; // commented out to fix a limitation in error sim: throw new
			// TftpAbortException("Cannot resend last packet");
		}

		try {
			socket.send(resendDatagram);
			IO.print("Resending last transfer packet.");
		} catch (IOException e) {
			throw new TFTPAbortException(e.getMessage());
		}
	}
    
    private TFTPPacket receive() throws IOException, TFTPAbortException {
		while (true) {
			
			socket.receive(rcvDatagram);
			
			if (Server.getVerbose()) {
				printPacketDetails(2, rcvDatagram);
			}
			
			if (remoteTid > 0 && (rcvDatagram.getPort() != remoteTid 
				|| !(rcvDatagram.getAddress()).equals(remoteAddress))) {
				IO.print("Port does not match error : "+ "remoteTid: "+remoteTid + " port: "+rcvDatagram.getPort() + " remoteAddress: "+ remoteAddress + "rcvDatagram.getAddress(): "+rcvDatagram.getAddress());
				//@TODO need to handle this case
				sendUnknownTidError(rcvDatagram.getAddress(),
						rcvDatagram.getPort());
				continue;
			}
			try {
				return TFTPPacket.createFromDatagram(rcvDatagram);
			} catch (IllegalArgumentException e) {
				sendIllegalOperationError(e.getMessage());
			}
		}
	}
    
    public TFTPDATAPacket receiveData(int blockNumber)
			throws TFTPAbortException {

    	TFTPDATAPacket pk = (TFTPDATAPacket) receiveExpected(
				TFTPPacket.Type.DATA, blockNumber);

		// Auto-set remoteTid, for convenience
		if (remoteTid <= 0 && blockNumber == 1) {
			setRemoteTid(rcvDatagram.getPort());
		}

		return pk;
	}
    
    public TFTPACKPacket receiveAck(int blockNumber) throws TFTPAbortException {
    	TFTPACKPacket pk = (TFTPACKPacket) receiveExpected(TFTPPacket.Type.ACK,
				blockNumber);

		// Auto-set remoteTid, for convenience
		if (remoteTid <= 0 && blockNumber == 0) {
			setRemoteTid(rcvDatagram.getPort());
		}
		
		return pk;
	}
    
    
    private TFTPPacket receiveExpected(TFTPPacket.Type type, int blockNumber) throws TFTPAbortException {
		int timeouts = 0;

		try {
			while (true) {
				try {

					TFTPPacket pk = receive();

					if (pk.getTFTPacketType() == type) {
						if (pk.getTFTPacketType() == TFTPPacket.Type.DATA) {
							TFTPDATAPacket dataPk = (TFTPDATAPacket) pk;
							if (dataPk.getBlockNumber() == blockNumber) {
								return dataPk;
							} else if(dataPk.getBlockNumber() < blockNumber) {
								// We received an old data packet, so send
								// corresponding ack
								echoAck(dataPk.getBlockNumber());
							} else {
								// Received future block, this is invalid
								sendIllegalOperationError("Received future data block number: "
										+ dataPk.getBlockNumber());
							}
						} else if (pk.getTFTPacketType() == TFTPPacket.Type.ACK) {
							TFTPACKPacket ackPk = (TFTPACKPacket) pk;
							if (ackPk.getBlockNumber() == blockNumber) {
								return pk;
							} else if (ackPk.getBlockNumber() > blockNumber) {
								sendIllegalOperationError("Received future ack block number: "
										+ ackPk.getBlockNumber());
							}
						}
					}else if (pk instanceof TFTPErrorPacket) {
						TFTPErrorPacket errorPk = (TFTPErrorPacket) pk;
						IO.print("Received error packet. Code: "
								+ errorPk.getCode() + ", Type: "
								+ errorPk.getErrorType().toString()
								+ ", Message: \"" + errorPk.getErrorMessage()
								+ "\"");

						if (errorPk.shouldAbortTransfer()) {
							IO.print("Aborting transfer");
							throw new TFTPAbortException(
									errorPk.getErrorMessage());
						} else {
							IO.print("Continuing with transfer");
						}
					} else if (pk instanceof TFTPRRQWRQPacket) {
						throw new TFTPAbortException(
								"Received request packet within data transfer connection");
						}
				}catch (SocketTimeoutException e) {
					if (timeouts >= maxResendAttempts) {
						throw new TFTPAbortException(
								"Connection timed out. Giving up.");
					}

					IO.print("Waiting to receive " + type + " #" + blockNumber
							+ " timed out, trying again.");

					timeouts++;
					resendLastPacket();
				}
			}
		} catch (IOException e) {
			throw new TFTPAbortException(e.getMessage());
		}
	}
    
    public void sendData(int blockNumber, byte[] fileData, int fileDataLength) throws TFTPAbortException {
		try {
			TFTPDATAPacket pk = TFTPPacket.createDataPacket(blockNumber,
					fileData, fileDataLength);

			send(pk, true);
			IO.print("sent: data #" + blockNumber
					+ ((pk.isLastDataPacket()) ? " (last)" : ""));
		} catch (Exception e) {

			throw new TFTPAbortException(e.getMessage());
		}
	}




    /**
     * the function used to receive packets and print out the packets
     * @param packet variable used to store received packets
     * @param socket socket used to receive packets
     * @return received packets
     */
    public static DatagramPacket receive(DatagramPacket packet, DatagramSocket socket) {

        if( socket.isClosed() ) {
            System.out.print("Socket is closed, unable to receive packets");
        }

        try {
            socket.receive(packet);

			//1     Read request (RRQ)
			//2     Write request (WRQ)
			if (Client.getVerbose()) {
				printPacketDetails(1, packet);
			}
        } catch ( SocketTimeoutException e){
            System.out.println("\nMax timeout reached, no packet received, closing socket...");
            socket.close();
            System.exit(1);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return packet;
    }
    
    public static void printPacketDetails(int type, DatagramPacket pk)
	{
		if (type == 1)
		{
			System.out.print(
					"\n**************************Received Packet Information**************************" +
							"\nPacket Type: " + getPacketType(pk) +
							"\nPacket Source: " + pk.getAddress() +
							"\nSource Port: " + pk.getPort() +
							"\nPacket Data(String): " + Arrays.toString(pk.getData()) +
							"\nPacket Data(Byte): " + pk.getData() +
							"\nPacket Offset: " + pk.getOffset() +
							"\n******************************************************************************\n")
			;
		} else if (type == 2)
		{
			System.out.print(
					"\n----------------------------Sent Packet Information------------------------" +
							"\nPacket Type: " + getPacketType(pk) +
							"\nPacket Destination: " + pk.getAddress() +
							"\nDestination Port: " + pk.getPort() +
							"\nPacket Data(String): " + Arrays.toString(pk.getData()) +
							"\nPacket Data(Byte): " + pk.getData() +
							"\nPacket Offset: " + pk.getOffset() +
							"\nSocket Address: " + pk.getSocketAddress() +
							"\n---------------------------------------------------------------------------\n"
			);
		}
	}



    private void sendIllegalOperationError(String message)
			throws TFTPAbortException {
		try {
			TFTPErrorPacket pk = TFTPPacket.createErrorPacket(
					TFTPErrorPacket.ErrorType.ILLEGAL_OPERATION, message);
			send(pk);
			System.out.println("Sending error packet (Illegal Operation) with message: "
					+ message);
			throw new TFTPAbortException(message);
		} catch (IOException e) {
			throw new TFTPAbortException(message);
		}
	}


	private void sendUnknownTidError(InetAddress address, int port) {
		try {
			String errMsg = "Stop hacking foo!";
			TFTPErrorPacket pk = TFTPPacket.createErrorPacket(
					TFTPErrorPacket.ErrorType.UNKOWN_TID, errMsg);
			socket.send(pk.generateDatagram(address, port));
			System.out.println("*******  Sending error packet (Unknown TID) to "
					+ addressToString(address, port) + " with message: "
					+ errMsg);
		} catch (Exception e) {
			// Ignore
		}
	}

	public void sendFileNotFound(String message) {
		try {
			TFTPErrorPacket pk = TFTPPacket.createErrorPacket(
					TFTPErrorPacket.ErrorType.FILE_NOT_FOUND, message);
			send(pk);
			System.out.println("Sending error packet (File not Found) with message: "
					+ message);
		} catch (IOException e) {
			// Ignore
		}
	}

	public void sendDiscFull(String message) {
		try {
			TFTPErrorPacket pk = TFTPPacket.createErrorPacket(
					TFTPErrorPacket.ErrorType.DISC_FULL_OR_ALLOCATION_EXCEEDED,
					message);
			send(pk);
			System.out.println("Sending error packet (Disc Full) with message: " + message);
		} catch (IOException e) {
			// Ignore
		}
	}

	public void sendAccessViolation(String message) {
		try {
			TFTPErrorPacket pk = TFTPPacket.createErrorPacket(
					TFTPErrorPacket.ErrorType.ACCESS_VIOLATION, message);
			send(pk);
			System.out.println("Sending error packet (Access Violation) with message: "
					+ message);
		} catch (IOException e) {
			// Ignore
		}
	}

	public void sendFileAlreadyExists(String message) {
		try {
			TFTPErrorPacket pk = TFTPPacket.createErrorPacket(
					TFTPErrorPacket.ErrorType.FILE_ALREADY_EXISTS, message);
			send(pk);
			System.out.println("Sending error packet (File Already Exists) with message: "
					+ message);
		} catch (IOException e) {
			// Ignore
		}
	}
	
	private String addressToString(InetAddress addr, int port) {
		return addr.toString() + ":" + port;
	}
    
    public void setRemoteAddress(InetAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public void setRemoteTid(int remoteTid) {
		this.remoteTid = remoteTid;
	}
	
	public void setRequestPort(int requestPort) {
		this.requestPort = requestPort;
	}

}
