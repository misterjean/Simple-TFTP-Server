package Utilities;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by Yue on 2016-09-19.
 * this is where proxy receive and forward packets
 * it implements Runnable thus it could be multi-threaded
 */
public class Proxy_PacketProcessor implements Runnable {

    /**
     * the port used to receive request packets from client
     */
    private static final int DEFAULT_CLIENT_PORT = 2300;

    /**
     * the port that server used to receive request packets from proxy
     */
    private static final int DEFAULT_SERVER_PORT = 69;

    /**
     * the port that client socket uses to send and receive
     */
    private int port_client;

    /**
     * the port that server socket uses to send and receive
     */
    private int port_server;

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

    /**
     * to store received data packets
     */
    private DatagramPacket dataPacket = PacketUtilities.createEmptyPacket();

    /**
     * to store received ack packets
     */
    private DatagramPacket ackPacket = PacketUtilities.createEmptyPacket();

    /**
     * this variable is used to flag whether a data packet is the last one
     * when last data packet has been received, it turns to true
     */
    private boolean isLast = false;

    /**
     * used to determine whether if any thread is receiving request packets
     * other threads should wait if the value of this variable is true
     */
    private static boolean isReceiving = false;

    /**
     * used to mark which type of request packet a thread received
     */
    private String requestPacketType = "";


    public static String getIsReceving(){
        if( isReceiving ) return "true";
        else return "false";
    }


    @Override
    public void run() {
        //increase ID_count and set the ID for the new instance
        this.ID = ++ID_count;

        IO.print("Packet Processor, ID: " + this.ID + " has started!");

        String stage = "request";

        boolean isRunning = true;
        while( isRunning ){
            switch( stage ){
                case "request":
                    //open socket_receive, and receive a request packet from client,
                    //and store it in requestPacket, and close socket_receive after
                    receiveRequestPacket();

                    //get client port from request packet
                    this.port_client = this.requestPacket.getPort();

                    //set the destination port of request packet to 69
                    this.requestPacket.setPort( DEFAULT_SERVER_PORT );

                    //open a socket_receSend
                    openSocketForReceiveAndSend();

                    //forward the received request packet to server
                    PacketUtilities.send(this.requestPacket, this.socket_receSend);

                    //request stage end
                    if( PacketUtilities.isRRQPacket(this.requestPacket) ) {
                        this.requestPacketType = "RRQ";
                        stage = "data";
                    }
                    else if( PacketUtilities.isWRQPacket(this.requestPacket) ) {
                        this.requestPacketType = "WRQ";
                        stage = "ack";
<<<<<<< HEAD:Iteration 1/src/Utilities/Proxy_PacketProcessor.java
=======
                        System.out.println("I am here");
>>>>>>> f85f99fb330d3f54fcf5edc2fae022486c4fa3bb:TFTP/src/Utilities/Proxy_PacketProcessor.java
                    }
                    else{
                        //error
                        IO.print("Error!");
                        //more code for handling error
                    }
                    break;
                case "data":
                    //receive data packet
                    if( this.requestPacketType.equals("RRQ") ){
                        //receive data packet from server
                        receiveDataPacket();

                        //get sever port
                        this.port_server = this.dataPacket.getPort();

                        //set the port to client port
                        this.dataPacket.setPort( this.port_client );
                    }
                    else if( this.requestPacketType.equals("WRQ") ){
                        //receive data packet from client
                        receiveDataPacket();

                        //set the port to server
                        this.dataPacket.setPort( this.port_server );
                    }
                    else{
                        //error packets
                        IO.print("This should never happen");
                        //more code
                    }

                    //forward data packet to client
                    sendDataPacket();

                    //check this data packet whether it's the last one
                    if( PacketUtilities.isLastPacket(this.dataPacket) ) this.isLast = true;

                    //data stage ends
                    stage = "ack";

                    break;
                case "ack":
                    if( this.requestPacketType.equals("RRQ") ){
                        //receive ack packet from client
                        receiveDataPacket();

                        //set the port to server
                        this.ackPacket.setPort( this.port_server );
                    }
                    else if( this.requestPacketType.equals("WRQ") ){
                        //receive ack packet from server
                        receiveAckPacket();

                        //get server port
                        this.port_server = ackPacket.getPort();

                        //set port to client port
                        this.ackPacket.setPort( this.port_client );
                    }
                    else{
                        //error packet
                        IO.print("This should never happen");
                        //more code
                    }
                    //forward ack packet to client
                    sendAckPacket();

                    //when last data has been processed
                    if( this.isLast ){
                        IO.print("Last data packet has been processed, file transfer completed!");
                        --ID_count;
                        isRunning = false;
                        break;
                    }

                    //ack stage ends
                    stage = "data";
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * this part opens socket_receive on port 23, receives request packets from client
     * this method is thread safe
     * after calling this method, a value should be assigned to requestPacket
     */
    private synchronized void receiveRequestPacket() {

        //open socket_receive
        try {
            if( isReceiving ) {
                IO.print( "Other packet processor is receiving, waiting...");
                wait();
            }

            isReceiving = true;

            socket_receive = new DatagramSocket( DEFAULT_CLIENT_PORT);


            IO.print("Waiting for request packets from client...");
        } catch (SocketException e) {
            IO.print("Unable to make socket listen on port" + DEFAULT_CLIENT_PORT );
            e.printStackTrace();
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        //receive request packet
        PacketUtilities.receive( this.requestPacket, socket_receive);

        //close socket_receive to finish receive
        socket_receive.close();
        isReceiving = false;
        notifyAll();
    }


    /**
     * this method opens a socket to receive and send packets
     * the socket listens on a random port
     * handles exception inside
     */
    private void openSocketForReceiveAndSend(){
        try {
            this.socket_receSend = new DatagramSocket();
        } catch (SocketException e) {
            IO.print("Unable to open a socket for receive and send. Exit...");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * this method sends data packet
     * catches exceptions inside
     */
    private void receiveDataPacket(){
            //this.socket_receSend.receive( this.dataPacket );
            PacketUtilities.receive(this.dataPacket, this.socket_receSend);

    }

    /**
     * this method receives data packet
     * catches exceptions inside
     */
    private void sendDataPacket(){
            //this.socket_receSend.send( this.dataPacket );
            PacketUtilities.send(this.dataPacket, this.socket_receSend);
    }
    /**
     * this method receives ack packet
     * catches exceptions inside
     */
    private void receiveAckPacket(){
            //this.socket_receSend.receive( this.ackPacket );
            PacketUtilities.receive(this.ackPacket, this.socket_receSend);
    }
    /**
     * this method sends ack packet
     * catches exceptions inside
     */
    private void sendAckPacket(){
            //this.socket_receSend.send( this.ackPacket );
<<<<<<< HEAD:Iteration 1/src/Utilities/Proxy_PacketProcessor.java
            PacketUtilities.send(this.ackPacket, this.socket_receSend);
    }
=======
    	    System.out.println("About to send ACK From Proxy");
    	    System.out.println("Proxy: "+ this.ackPacket.getData() +" port: " + this.ackPacket.getPort()+"Address: "+ this.ackPacket.getAddress());
    	    System.out.println("socket_receSend: "+ this.socket_receSend);
    	    //Fucking read your code next time, and listen to others when they say your code is the one that causing the issue. we are all fucking 
    	    //learn from our mistake, no one is perfect, humble yourself.
    	    try {
				this.ackPacket.setAddress(InetAddress.getLocalHost());
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	    
            PacketUtilities.send(this.ackPacket, this.socket_receSend);
>>>>>>> f85f99fb330d3f54fcf5edc2fae022486c4fa3bb:TFTP/src/Utilities/Proxy_PacketProcessor.java
}
}