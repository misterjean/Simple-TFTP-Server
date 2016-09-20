package utilities;

import javax.xml.crypto.Data;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by Yue on 2016-09-19.
 * this is where proxy receive and forward packets
 * it implements Runnable thus it could be multi-threaded
 */
public class Proxy_PacketProcessor implements Runnable {

    /**
     * the port used to receive request packets from client
     */
    private static final int DEFAULT_CLIENT_PORT = 23;

    /**
     * the port that client socket
     */
    private int port_client;

    /**
     * the unique ID for each packet processor
     */
    private int ID;

    /**
     * to count how many packet processors have been created
     * it starts at 0
     * every time a new instance of Proxy_PacketProcessor is created, it increases by 1
     */
    private static int ID_count = 0;

    /**
     * the socket used to receive request packets only from client
     * by default it should listen on port 23
     */
    private static DatagramSocket socket_receive;

    /**
     * the socket used to forward and receive other packets between client and server
     * it listens on a random port
     */
    private DatagramSocket socket_receSend;

    /**
     * the variable used to store received request packets
     */
    private DatagramPacket requestPacket = PacketUtilities.createEmptyPacket();


    @Override
    public void run() {

        //increase ID_count and set the ID for the new instance
        this.ID = ++ID_count;

        IO.print("Packet Processor, ID: " + this.ID + " has started!");

        //receive a request packet from client and store it in requestPacket
        receiveRequestPacket();

        //get client port from request packet
        port_client = requestPacket.getPort();



    }

    /**
     * this part opens socket_receive on port 23, receives request packets from client
     * this method is thread safe
     * after calling this method, a value should be assigned to requestPacket
     */
    private synchronized void receiveRequestPacket(){
        //open socket_receive
        try {
            socket_receive = new DatagramSocket( DEFAULT_CLIENT_PORT);

            IO.print("Waiting for request packets from client...");
        } catch (SocketException e) {
            System.out.print("Unable to make socket listen on port" + DEFAULT_CLIENT_PORT );
            e.printStackTrace();
        }

        //receive request packet
        PacketUtilities.receive( this.requestPacket, socket_receive);

        //close socket_receive to finish receive
        socket_receive.close();
    }


}
