package Utilities;

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

    /**
     * an empty buffer for packets
     */
    public static byte[] rawData = new byte[DEFAULT_DATA_LENGTH];

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
            IO.print("Socket is closed, unable to send packets");
        }

        try {
            socket.send(packet);

			String data_string = new String( packet.getData() );

            IO.print(
                    "\n----------------------------Sent Packet Information------------------------" +
                            "\nPacket Type: " + getPacketType(packet) +
                            "\nPacket Destination: " + packet.getAddress() +
                            "\nDestination Port: " + packet.getPort() +
                            "\nPacket Data(Byte): "+ Arrays.toString( packet.getData() ) +
							"\nPacket Data(string): " +  data_string +
							"\nPacket Offset: " + packet.getOffset() +
                            "\nSocket Address: " + packet.getSocketAddress() +
                            "\n---------------------------------------------------------------------------\n"
            );

        } catch (IOException e) {
            e.printStackTrace();
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

			String data_string = new String( packet.getData() );

            System.out.print(
                    "\n**************************Received Packet Information**************************" +
                            "\nPacket Type: " + getPacketType(packet) +
                            "\nPacket Source: " + packet.getAddress() +
                            "\nSource Port: " + packet.getPort() +
                            "\nPacket Data(Byte): "+ Arrays.toString( packet.getData() ) +
							"\nPacket Data(string): " +  data_string +
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


	public static void printPacket( DatagramPacket packet ){
		IO.print(
				"\n----------------------------Packet Information------------------------" +
						"\nPacket Type: " + getPacketType(packet) +
						"\nPacket Destination: " + packet.getAddress() +
						"\nDestination Port: " + packet.getPort() +
						"\nPacket Data(String): "+ Arrays.toString( packet.getData() ) +
						"\nPacket Data(Byte): " + packet.getData() +
						"\nPacket Offset: " + packet.getOffset() +
						"\nSocket Address: " + packet.getSocketAddress() +
						"\n---------------------------------------------------------------------------\n"
		);
	}
}
