package Utilities;

import java.io.ByteArrayOutputStream;

public class TFTPRRQWRQPacket extends TFTPPacket {
	private static final int MINLENGTH = 10; //we dont want the file name to be less then this
	// Types of actions for the request
	public static enum Action {
		READ, WRITE
	}

	// Option for the mode of transfer
	public static enum Mode {
		ASCII, OCTET
	}
	
	private String fileName = ""; // filename of the file
	private Action action; // read or write
	private Mode mode; // mode type ascii or octet
	
	TFTPRRQWRQPacket(String fileName, Action action, Mode mode) throws IllegalArgumentException {
		// filename, action and mode should not be null
		if (fileName == null || fileName.length() == 0 || action == null || mode == null) {
			String message = "Missing data in the request packet";
			if (fileName == null || fileName.length() == 0) {
				message = "Missing file name";
			}else if (action == null) {
				message = "Not a read or write request";
			}else if (mode == null) {
				message = "Invalid transfer mode";
			}
			throw new IllegalArgumentException(message);
		}
		this.fileName = fileName;
		this.action = action;
		this.mode = mode;
		this.type = (action == Action.READ) ? Type.RRQ : Type.WRQ;
	}
	
	/* 
	 * get filename method
	 */
	public String getFilename() {
		return fileName;
	}
	
	/*
	 * Check if this is a read request
	 */
	public boolean isReadRequest() {
		return (action == Action.READ);
	}

	/*
	 * (non-Javadoc)
	 * @see Utilities.TFTPPacket#generateData()
	 */
	static TFTPRRQWRQPacket createFromBytes(byte[] packetData, int packetLength) throws IllegalArgumentException {
		Action action;
		String filename;
		Mode mode;

		// Check that the Data is not null and is long enough
		if (packetData == null || packetData.length < packetLength || packetLength < MINLENGTH) {
			throw new IllegalArgumentException("Data is not long enough");
		}

		// Check if opcode is valid
		if (packetData[0] != 0) {
			throw new IllegalArgumentException("Invalid OP code");
		} else if (packetData[1] == 1) {
			action = Action.READ;
		} else if (packetData[1] == 2) {
			action = Action.WRITE;
		} else {
			int opcode = ((packetData[0] << 8) & 0xFF00) | (packetData[1] & 0xFF);
			throw new IllegalArgumentException("Invalid OP code: " + opcode);
		}

		// Check fileName
		int i = 1;
		StringBuilder filenameBuilder = new StringBuilder();
		while (packetData[++i] != 0 && i < packetLength) {
			filenameBuilder.append((char) packetData[i]);
		}
		filename = filenameBuilder.toString();

		// Must have 0 after filename
		if (packetData[i] != 0) {
			throw new IllegalArgumentException("Must have 0 after filename");
		}

		// Check the transfer mode
		StringBuilder modeStrBuilder = new StringBuilder();
		while (packetData[++i] != 0 && i < packetLength) {
			modeStrBuilder.append((char) packetData[i]);
		}
		
		// Save the transfer mode
		String modeStr = modeStrBuilder.toString().toLowerCase();
		if (modeStr.equals("netascii")) {
			mode = Mode.ASCII;
		} else if (modeStr.equals("octet")) {
			mode = Mode.OCTET;
		} else {
			String errMsg;
			if (modeStr == null || modeStr.isEmpty()) {
				errMsg = "Missing transfer mode";
			} else {
				errMsg = "Invalid transfer mode: " + modeStr;
			}
			throw new IllegalArgumentException(errMsg);
		}

		// Check for the terminating 0 and make sure there is no more data
		if (packetData[packetLength - 1] != 0) {
			throw new IllegalArgumentException(
					"Trailing 0 not found after mode");
		}

		// Create a RequestPacket
		return new TFTPRRQWRQPacket(filename, action, mode);
}

	@Override
	public byte[] generateData() {
		// TODO Auto-generated method stub
		// Form the byte array
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		stream.write(0); // Always start with 0
		
		// Set the request action type byte
		IO.print("Action "+ action);
		if (action == Action.WRITE) {
			stream.write(2); // write request flag byte
		} else {
			stream.write(1); // read request flag byte
		}
		
		// Add filename and mode (along with terminating strings)
		byte[] tempByteArr = fileName.getBytes();
		stream.write(tempByteArr, 0, tempByteArr.length);
		stream.write(0);
		
		tempByteArr = mode.toString().toLowerCase().getBytes();
		stream.write(tempByteArr, 0, tempByteArr.length);
		stream.write(0);
		
		// Convert to byte array and return
		return stream.toByteArray();
	}

}
