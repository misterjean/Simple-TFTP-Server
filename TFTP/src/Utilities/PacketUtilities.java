package Utilities;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
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

	private int requestPort = 9000; //default request port over 9000!
	private int remoteTid = -1;
	private DatagramPacket rcvDatagram = TFTPPacket.createDatagramForReceiving();
	private DatagramPacket sendDatagram;
	
	
	
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
    	
		IO.print("IN LastSEND: "+ packet +" socket: "+ socket);


        if( socket.isClosed() ){
            System.out.println("Socket is closed, unable to send packets");
        }

        try {
    		IO.print("Last SEND: "+ packet + ": "+ packet);

            socket.send(packet);

            System.out.print(
                    "\n----------------------------Sent Packet Information------------------------" +
                            "\nPacket Type: " + getPacketType(packet) +
                            "\nPacket Destination: " + packet.getAddress() +
                            "\nDestination Port: " + packet.getPort() +
                            "\nPacket Data(String): "+ Arrays.toString( packet.getData() ) +
                            "\nPacket Data(Byte): " + packet.getData() +
                            "\nPacket Offset: " + packet.getOffset() +
                            "\nSocket Address: " + packet.getSocketAddress() +
                            "\n---------------------------------------------------------------------------\n"
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendRequest(TFTPRRQWRQPacket packet) throws IOException {
    	sendDatagram = packet.generateDatagram(remoteAddress, requestPort);
    	
		send(sendDatagram, socket);
		
	}

    
    private void send(TFTPPacket packet) throws IOException {
		DatagramPacket dp = packet.generateDatagram(remoteAddress, remoteTid);
		IO.print("IN SEND: "+ packet +" remoteTid: "+ remoteTid);

		send(dp, socket);
	}
    
    public void sendAck(int blockNumber) throws TFTPAbortException {
		try {
			send(TFTPPacket.createACKPAcket(blockNumber));
		} catch (Exception e) {
			throw new TFTPAbortException(e.getMessage());
		}
	}
    
    private TFTPPacket receive() throws IOException, TFTPAbortException {
		while (true) {
			IO.print("IN RECEIVE");
			IO.print(" After IN RECEIVE "+ "local: " +socket.getLocalPort()+ "destPort: " +socket.getPort());
			socket.receive(rcvDatagram);
			IO.print(" After IN RECEIVE "+ "local: " +socket.getLocalPort()+ "destPort: " +socket.getPort());


			if (remoteTid > 0 && (rcvDatagram.getPort() != remoteTid 
				|| !(rcvDatagram.getAddress()).equals(remoteAddress))) {
				IO.print("Port does not match error : "+ "remoteTid: "+remoteTid + " port: "+rcvDatagram.getPort() + " remoteAddress: "+ remoteAddress + "rcvDatagram.getAddress(): "+rcvDatagram.getAddress());
				//@TODO need to handle this case
				continue;
			}

			try {
				return TFTPPacket.createFromDatagram(rcvDatagram);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}
    
    public TFTPDATAPacket receiveData(int blockNumber)
			throws TFTPAbortException {
    	IO.print("IN RCVDATA");
    	TFTPDATAPacket pk = (TFTPDATAPacket) receiveExpected(
				TFTPPacket.Type.DATA, blockNumber);
    	IO.print("AFter pk");

		// Auto-set remoteTid, for convenience
		if (remoteTid <= 0 && blockNumber == 1) {
			setRemoteTid(rcvDatagram.getPort());
		}
    	IO.print("Before return");
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
    	IO.print("receiveExpected");
		try {
			while (true) {
				try {
			    	IO.print("receiveExpected try");

					TFTPPacket pk = receive();
			    	IO.print("receiveExpected try receive");


					if (pk.getTFTPacketType() == type) {
						if (pk.getTFTPacketType() == TFTPPacket.Type.DATA) {
							TFTPDATAPacket dataPk = (TFTPDATAPacket) pk;
							if (dataPk.getBlockNumber() == blockNumber) {
						    	IO.print("receiveExpected if");
								return dataPk;
							} else {
							//@TODO handle this case for Received future block
							}
						} else if (pk.getTFTPacketType() == TFTPPacket.Type.ACK) {
							TFTPACKPacket ackPk = (TFTPACKPacket) pk;
							if (ackPk.getBlockNumber() == blockNumber) {
								return pk;
							} else if (ackPk.getBlockNumber() > blockNumber) {
								//@TODO handle this case for Received future ACK

							}
						}
					}
				}catch (SocketTimeoutException e) {
					e.printStackTrace();
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
			IO.print("IN SENDDATA");
			send(pk);
		} catch (Exception e) {
			IO.print(" ERROR IN SENDDATA");
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

            System.out.print(
                    "\n**************************Received Packet Information**************************" +
                            "\nPacket Type: " + getPacketType(packet) +
                            "\nPacket Source: " + packet.getAddress() +
                            "\nSource Port: " + packet.getPort() +
                            "\nPacket Data(String): "+ Arrays.toString( packet.getData() ) +
                            "\nPacket Data(Byte): " + packet.getData() +
                            "\nPacket Offset: " + packet.getOffset() +
                            "\n******************************************************************************\n" )
            ;

        } catch ( SocketTimeoutException e){
            System.out.println("\nMax timeout reached, no packet received, closing socket...");
            socket.close();
            System.exit(1);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return packet;
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
