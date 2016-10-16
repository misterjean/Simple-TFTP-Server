package Utilities;

import java.io.ByteArrayOutputStream;

public class TFTPDATAPacket extends TFTPPacket {
	private static final int OPCODE = 3; // The TFTP opcode
	
	public static final int MAXFILEDATALENGTH = 512; //Max file data length
	
	private static final int MINBLOCKNUMBER = 1; // minimum block number
	private static final int MAXBLOCKNUMBER = 0xFFFF; // maximum block number

	// length needed for header (also is minimum length of data packet)
	private static final int PACKETHEADERLENGTH = 4;
	
	// block number for the packet
	private int blockNumber = 0;
	
	// data byte array from the file being read/written
	private byte[] fileData = null;
	
	
	TFTPDATAPacket(int blockNumber, byte[] fileData, int fileDataLength){
		if (blockNumber < MINBLOCKNUMBER || blockNumber > MAXBLOCKNUMBER ) {
			throw new IllegalArgumentException("Invalid block number");
		}
		
		if ( fileData == null && fileDataLength != 0 ) {
			throw new IllegalArgumentException(
					"Data length must be 0 if data is null");
		}
		
		if (fileData != null && (fileDataLength > fileData.length
			|| fileDataLength > MAXFILEDATALENGTH || fileDataLength < 0)) {
			throw new IllegalArgumentException("Invalid data length passed");
		}
		
		this.type = Type.DATA;
		this.blockNumber = blockNumber;
		if (fileData == null || fileDataLength == 0) {
			this.fileData = new byte[0];
		} 
		else {
			this.fileData = new byte[fileDataLength];
			System.arraycopy(fileData, 0, this.fileData, 0, fileDataLength);
		}
	}
	
	/*
	 * Get method for FileData
	 */
	public byte[] getFileData() {
		return fileData;
	}
	
	/*
	 * Get block number
	 */
	public int getBlockNumber() {
		return blockNumber;
	}
	
	
	/*
     * to determine whether the target packet is the last packet or not
     * the last packet will contain less than 512 bytes in its buffer
	 */
	public boolean isLastDataPacket() {
		return (fileData.length < MAXFILEDATALENGTH);
	}
	
	/*
	 * cretae the packet Data
	 */
	static TFTPDATAPacket createFromBytes(byte[] packetData, int packetLength)
			throws IllegalArgumentException {
		// Make sure we don't have null
		if (packetData == null) {
			throw new IllegalArgumentException("No valid data found");
		}

		// Verify packet length is valid
		if (packetLength > packetData.length
				|| packetLength < PACKETHEADERLENGTH
				|| packetLength > TFTPPacket.MAXLENGTH) {
			throw new IllegalArgumentException("Invalid packet length");
		}

		// Verify opcode
		if (packetData[0] != 0 || packetData[1] != OPCODE) {
			throw new IllegalArgumentException("Invalid opcode");
		}

		// Extract the file data and block number
		int blockNumber = ((packetData[2] << 8) & 0xFF00)
				| (packetData[3] & 0xFF);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		stream.write(packetData, PACKETHEADERLENGTH, packetLength-PACKETHEADERLENGTH);
		packetData = stream.toByteArray();
		return new TFTPDATAPacket(blockNumber, packetData, packetData.length);
	}

	


	@Override
	public byte[] generateData() {
		// TODO Auto-generated method stub
		ByteArrayOutputStream stream = new ByteArrayOutputStream(); //buffer of 32 byte.
		stream.write(0);
		stream.write(OPCODE);
		stream.write(blockNumber >> 8);
		stream.write(blockNumber);
		stream.write(fileData, 0, fileData.length);
		
		/*
		 *  Let check that we are sending the right file content for .txt file
		 */
		byte b [] = stream.toByteArray();
	      System.out.println("Print the content");
	      
	      for(int x = 0; x < b.length; x++) {
	         // printing the characters
	         System.out.print((char)b[x]  + "   "); 
	      }
	      System.out.println("   ");// remove this later

		return stream.toByteArray();
	}
	

}
