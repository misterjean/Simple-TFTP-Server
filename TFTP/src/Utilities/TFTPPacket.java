package Utilities;
import java.net.DatagramPacket;
import java.net.InetAddress;

import Utilities.TFTPRRQWRQPacket.Action;
import Utilities.TFTPRRQWRQPacket.Mode;

public abstract class TFTPPacket {
	static final int MAXLENGTH = PacketUtilities.DEFAULT_DATA_LENGTH;
	private static final int MINLENGTH = 4;
	
	public enum Type {
		RRQ, WRQ, DATA, 
		ACK, ERROR
	}
	
	Type type;
	
	public Type getTFTPacketType() {
		return type;
	}
	
	
	//@TODO We need read request
	public static TFTPRRQWRQPacket createReadRequestPacket(String fileName, Mode mode) {
		
		return new TFTPRRQWRQPacket(fileName, Action.READ, mode);
		
	}
	
	
	//@TODO we need a write request
	public static TFTPRRQWRQPacket createWriteRequestPacket(String fileName, Mode mode) {
		
		return new TFTPRRQWRQPacket(fileName, Action.WRITE, mode);
		
	}
	
	//@TODO we need a ack packet
	public static TFTPACKPacket createACKPAcket(int blockLength) {
		
		return new TFTPACKPacket(blockLength);
		
	}
	
	//@TODO we need a data packet
	public static TFTPDATAPacket createDataPacket(int blockNumber, byte[] data,
			int dataLength) {
		return new TFTPDATAPacket(blockNumber, data, dataLength);
	}
	
	/*
	 * Create TFTP from the rcv Datagram
	 */
	public static TFTPPacket createFromDatagram(DatagramPacket datagram)
			throws IllegalArgumentException {
		return TFTPPacket.createFromBytes(datagram.getData(),
				datagram.getLength());
	}
	
	public static TFTPErrorPacket createErrorPacket(
			TFTPErrorPacket.ErrorType errorType, String errorMessage) {
		return new TFTPErrorPacket(errorType, errorMessage);
	}
	
	/*
	 * 
	 * Create the appropriate packet
	 */
	private static TFTPPacket createFromBytes(byte[] packetData, int packetLength) throws IllegalArgumentException {
		// Check that the packet length makes sense and is long enough
		if (packetData.length < packetLength || packetLength < MINLENGTH) {
			throw new IllegalArgumentException(
					"packet Length is less than minimum length");
		}

		// First should always be 0
		if (packetData[0] != 0) {
			throw new IllegalArgumentException("Invalid opcode");
		}

		switch (packetData[1]) {
		case 1:
			return TFTPRRQWRQPacket.createFromBytes(packetData, packetLength); //RRQ
		case 2:
			return TFTPRRQWRQPacket.createFromBytes(packetData, packetLength); //WRQ
		case 3:
			return TFTPDATAPacket.createFromBytes(packetData, packetLength); //DATA
		case 4:
			return TFTPACKPacket.createFromBytes(packetData, packetLength);  // ACK
		case 5:
			return TFTPErrorPacket.createFromBytes(packetData, packetLength);  // Error
		default:
			throw new IllegalArgumentException("Invalid opcode");
		}
	}
	
	/*
	 *  Method to createDatagram for Receiving
	 */
	public static DatagramPacket createDatagramForReceiving() {
		return new DatagramPacket(new byte[MAXLENGTH], MAXLENGTH);
	}
	
	/*
	 * Prepare a Datagram packet to send over
	 */
	public DatagramPacket generateDatagram(InetAddress remoteAddress, int remotePort) {
		byte data[] = this.generateData();
		return new DatagramPacket(data, data.length, remoteAddress, remotePort);
	}


	public abstract byte[] generateData();

}
