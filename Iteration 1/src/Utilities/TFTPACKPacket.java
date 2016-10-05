package Utilities;

public class TFTPACKPacket extends TFTPPacket {
	private static final int OPCODE = 4; // The TFTP opcode
	private static final int PACKETLENGTH = 4; // max length of packet
    //The correct maximum UDP message size is 65507, as determined by the 
	//following formula: 0xffff - (sizeof(IP Header) + sizeof(UDP Header)) = 65535-(20+8) = 65507
	private static final int MAXBLOCKNUMBER = 0xffff;
	private static final int MINBLOCKNUMBER = 0;
	private int blockNumber = 0;
	
	TFTPACKPacket(int blockNumber) throws IllegalArgumentException {
		if ( blockNumber < MINBLOCKNUMBER || blockNumber > MAXBLOCKNUMBER) {
			throw new IllegalArgumentException("Invalid block length");
		}
		this.blockNumber = blockNumber;
		this.type = Type.ACK;
		
	}
	
	/*
	 * cretae the packet Data
	 */
	
	public static TFTPACKPacket createFromBytes(byte[] packetData, int packetLength) throws IllegalArgumentException {
		//check packet length
		if (packetData == null || packetData.length  < PACKETLENGTH || packetLength != PACKETLENGTH ) {
			throw new IllegalArgumentException("Incorrect packet length");
		}
		//check opcode
		if (packetData[0] != 0 || packetData[1] != OPCODE ) {
			throw new IllegalArgumentException("Incorrect opcode");

		}
		 //shift packetData[2] to the left by 8 bits and let mask the variable so it leave only the value in the last 8 bits
		int blockLength = ((packetData[2] << 8) & 0xFF00)
				| (packetData[3] & 0xFF);
		return new TFTPACKPacket(blockLength);
	}
	
	/*
	 * Get BlockLegth
	 */
	public int getBlockNumber() {
		return blockNumber;
	}

	/*
	 * Generate Packet Data
	 */
	@Override
	public byte[] generateData() {
		byte[] data = new byte[4];
		data[0] = 0;
		data[1] = (byte) OPCODE;
		data[2] = (byte) (blockNumber >> 8);
		data[3] = (byte) (blockNumber);
		return data;
	}

}

