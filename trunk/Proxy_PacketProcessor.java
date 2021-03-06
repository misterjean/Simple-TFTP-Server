package Utilities;

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
    private static final int DEFAULT_CLIENT_PORT = 2300;

    /**
     * getter for DEFAULT_CLIENT_PORT
     * @return DEFAULT_CLIENT_PORT
     */
    public static int getDefaultClientPort() {return DEFAULT_CLIENT_PORT;}

    /**
     * the port that server used to receive request packets from proxy
     */
    private static final int DEFAULT_SERVER_PORT = 6900;

    /**
     * getter for DEFAULT_SERVER_PORT
     * @return DEFAULT_SERVER_PORT
     */
    public static int getDefaultServerPort() { return DEFAULT_SERVER_PORT; }
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
     * getter for ID
     * @return ID
     */
    public int getID() { return this.ID; }

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
     * to determine whether proxy should generate any error or not
     */
    private static boolean errMode = false;

    /**
     * to store error simulating setting
     * for opcode part
     */
    private static int err_opcode = 0;

    /**
     * to store error simulating setting
     * for block number
     */
    private static int err_blockNum = 0;

    /**
     * to store error simulating setting
     * to determine which error will be generated
     * 1 - lose a packet
     * 2 - delay a packet
     * 3 - duplicate a packet
     */
    private static int errCode = 0;

    /**
     * to store error simulating setting
     * to determine how long a packet will be delayed for
     * in millisecond
     */
    private static int delayTime = 0;

    /**
     * setter for errMode
     * @param bool value will be passed to errMode
     */
    public static void setErrMode(boolean bool){ errMode = bool; }

    /**
     * getter for errMode
     * @return errMode
     */
    public static boolean getErrMode() { return errMode; }

    /**
     * setter for err_opcode
     * @param i value will be passed to err_opcode
     */
    public static void setErr_opcode( int i ){ err_opcode = i; }

    /**
     * getter for err_opcode
     * @return err_opcode
     */
    public static int getErr_opcode() { return err_opcode; }

    /**
     * setter for err_blockNum
     * @param i value will be passed to err_blockNum
     */
    public static void setErr_blockNum( int i ){ err_blockNum = i; }

    /**
     * getter for err_blockNum
     * @return err_blockNum
     */
    public static int getErr_blockNum() { return err_blockNum; }

    /**
     * getter for errCode
     * @return errCode
     */
    public static int getErrCode() { return errCode; }

    /**
     * setter for errCode
     * @param i value will be passed to errCode
     */
    public static void setErrCode( int i ) { errCode = i; }

    /**
     * getter for delayTime
     * @return delayTime
     */
    public static int getDelayTime() { return delayTime; }

    /**
     * setter for delayTime
     * @param i value will be passed to delayTime
     */
    public static void setDelayTime( int i ) { delayTime = i; }

    /**
     * used to determine whether if any thread is receiving request packets
     * other threads should wait if the value of this variable is true
     */
    private static boolean isReceiving = false;

    /**
     * used to mark which type of request packet a thread received
     */
    private String requestPacketType = "";

    /**
     * getter for isReceving
     * @return isReceving
     */
    public static String getIsReceving(){
        if( isReceiving ) return "true";
        else return "false";
    }


    private static DatagramPacket duplicatedPacket = PacketUtilities.createEmptyPacket();

    private static boolean packetX2 = false;

    @Override
    public void run() {
        //increase ID_count and set the ID for the new instance
        this.ID = ++ID_count;

        IO.print( "Packet Processor, ID: " + this.ID + " has started!" );

        String stage = "request";

        boolean isRunning = true;
        while( isRunning ){
            switch( stage ){
                case "request":
                    /*
                    open socket_receive, and receive a request packet from client,
                    and store it in requestPacket, and close socket_receive after
                     */
                    receiveRequestPacket();

                    /*
                    get client port from request packet
                     */
                    this.port_client = this.requestPacket.getPort();

                    /*
                    open a socket_receSend
                     */
                    openSocketForReceiveAndSend();

                    /*
                    set the destination port of request packet to 69
                     */
                    this.requestPacket.setPort( DEFAULT_SERVER_PORT );

                    /*
                    error sim
                     */
                    if( errMode ) errorSim( this.requestPacket );

                    /*
                    forward the received request packet to server
                     */
                    //PacketUtilities.send(this.requestPacket, this.socket_receSend);

                    /*
                    request stage end
                     */
                    if( PacketUtilities.isRRQPacket(this.requestPacket) ) {
                        this.requestPacketType = "RRQ";
                        stage = "data";
                    }
                    else if( PacketUtilities.isWRQPacket(this.requestPacket) ) {
                        this.requestPacketType = "WRQ";
                        stage = "ack";
                    }
                    else{
                        //error
                        IO.print("Error!");
                        //more code for handling error
                    }

                    break;
                case "data":
                    /*
                    receive data packet
                     */
                    receiveDataPacket();

                    if( packetX2 ){
                        IO.printSimErrMsg( "This is the second time " + PacketUtilities.getPacketName( duplicatedPacket ) + " being sent.");
                        PacketUtilities.send(duplicatedPacket, this.socket_receSend);
                        packetX2 = false;
                    }

                    /*
                    err sim
                     */
                    if( errMode ) errorSim( this.dataPacket );

                    //forward data packet to client
                    sendDataPacket();

                    //check this data packet whether it's the last one
                    if( PacketUtilities.isLastPacket(this.dataPacket) ) this.isLast = true;

                    //data stage ends
                    stage = "ack";

                    break;
                case "ack":
                    //receive ack packet
                    receiveAckPacket();

                    if( packetX2 ){
                        IO.printSimErrMsg( "This is the second time " + PacketUtilities.getPacketName( duplicatedPacket ) + " being sent.");
                        PacketUtilities.send(duplicatedPacket, this.socket_receSend);
                        packetX2 = false;
                    }

                    //err sim
                    if( errMode ) errorSim( this.ackPacket );

                    //forward ack packet to client
                    sendAckPacket();

                    //when last data has been processed
                    if( this.isLast ){
                        IO.print("Last data packet has been processed, file transfer completed!");
                        IO.print("Press 'enter' to continue");
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
        try {
            /*
            wait if another thread is already trying to receive
             */
            while ( isReceiving ) {
                IO.print( "Another packet processor is receiving, packet processor #"
                        + this.ID + " is waiting... "
                        + "\n  -Press 'enter' to enter more commands-");
                wait();
            }

            /*
            inform other threads to wait
             */
            isReceiving = true;

            /*
            open a new socket that only receives request packets
             */
            socket_receive = new DatagramSocket( DEFAULT_CLIENT_PORT );

            /*
            inform user that the socket is ready
             */
            IO.print("Packet processor #" + this.ID + " is trying to receive a request packet from client..."
                    + "\n  -Press 'enter' to enter more command-");
        } catch (SocketException e) {
            IO.print("Unable to make socket listen on port" + DEFAULT_CLIENT_PORT );
            e.printStackTrace();
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        /*
        receive request packet
         */
        PacketUtilities.receive( this.requestPacket, socket_receive);

        /*
        close socket_receive to finish receive
         */
        socket_receive.close();

        /*
        inform other threads that no more waiting
         */
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
        if( this.requestPacketType.equals("RRQ") ){
            //receive data packet from server
            PacketUtilities.receive(this.dataPacket, this.socket_receSend);

            //get server port
            this.port_server = this.dataPacket.getPort();

            //set the port to client port
            this.dataPacket.setPort( this.port_client );
        }
        else if( this.requestPacketType.equals("WRQ") ){
            //receive data packet from client
            PacketUtilities.receive(this.dataPacket, this.socket_receSend);


            //set the port to server
            this.dataPacket.setPort( this.port_server );
        }
        else{
            //error packets
            IO.print("This should never happen");
            //more code
        }
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
        if( this.requestPacketType.equals("RRQ") ){
            //receive ack packet from client
            PacketUtilities.receive(this.ackPacket, this.socket_receSend);

            //set the port to server
            this.ackPacket.setPort( this.port_server );

            //set the address
            this.ackPacket.setAddress( this.requestPacket.getAddress() );
        }
        else if( this.requestPacketType.equals("WRQ") ){
            //receive ack packet from server
            PacketUtilities.receive(this.ackPacket, this.socket_receSend);

            //get server port
            this.port_server = this.ackPacket.getPort();

            //set port to client port
            this.ackPacket.setPort( this.port_client );
        }
        else{
            //error packet
            IO.print("This should never happen");
            //more code
        }
    }

    /**
     * this method sends ack packet
     * catches exceptions inside
     */
    private void sendAckPacket(){
        //this.socket_receSend.send( this.ackPacket );
        PacketUtilities.send(this.ackPacket, this.socket_receSend);
    }

    /**
     * This method is used to generate errors on the target packet
     * @param packet the target packet
     */
    private void errorSim(DatagramPacket packet){
        if( (err_opcode  == 1 || err_opcode == 2) && compareOpcode(packet) ){ //for RRQ and WRQ
            switch( errCode ){
                case 1: //lose a packet
                    IO.printSimErrMsg(PacketUtilities.getPacketType( packet ) +
                            " will be lost. Proxy will re-try to receive another one." );
                    receiveRequestPacket();

                    //set the destination port of request packet to 69
                    this.requestPacket.setPort( DEFAULT_SERVER_PORT );

                    break;
                case 2: //delay a packet
                    IO.printSimErrMsg(PacketUtilities.getPacketType( packet ) +
                            " will be delayed for " + delayTime + " milliseconds(" + delayTime/1000 + " seconds).");
                    delayPacket();
                    break;
                case 3: //duplicate a packet
                    /*
                    duplicate the packet and store it in another variable
                     */
                    duplicatedPacket = packet;
                    /*
                    Inform user what will happen
                     */
                    IO.printSimErrMsg(PacketUtilities.getPacketType( duplicatedPacket ) +
                            " will be duplicated. ");

                    packetX2 = true;
                    IO.printSimErrMsg( "This is the first time " + PacketUtilities.getPacketName( packet ) + " being sent.");
                    break;
                default:
                    IO.print("This should never happen!!!!!");
                    break;
            }
        }
        else if( (err_opcode == 3 || err_opcode == 4) && compareOpcode(packet) ){ //for DATA and WRQ
            if( compareBlockNum(packet) ){
                switch( errCode ){
                    case 1: //lose a packet
                        IO.printSimErrMsg(PacketUtilities.getPacketType( packet ) +
                                " will be lost. Proxy will re-try to receive another one." );

                        //repeat the receiving process
                        if( PacketUtilities.getPacketID( packet ) == 3){ receiveDataPacket(); } //DATA
                        else{ receiveAckPacket(); } //ACK
                        break;
                    case 2: //delay a packet
                        IO.printSimErrMsg(PacketUtilities.getPacketType( packet ) +
                                " will be delayed. "  );
                        delayPacket();
                        break;
                    case 3: //duplicate a packet
                        IO.printSimErrMsg(PacketUtilities.getPacketType( packet ) +
                                " will be duplicated. ");
                        /*
                        duplicate the packet and store it in another variable
                        */
                        duplicatedPacket = packet;

                        packetX2 = true;

                        IO.printSimErrMsg( "This is the first time " + PacketUtilities.getPacketName( packet ) + " being sent.");
                        break;
                    default:
                        IO.print("This should never happen!!!!!");
                        break;
                }
            }
        }
        else if( err_opcode <= 0 || err_opcode > 4) {
            IO.print( " This should never happen!!!!! ");
        }
    }
    /**
     * This method is used to compare the opcode of a packet with err_opcode
     * @param packet the target pacekt
     * @return true if the opcode of the packet matches err_opcode, false otherwise
     */
    private boolean compareOpcode( DatagramPacket packet ){ return packet.getData()[1] == err_opcode; }

    /**
     * This method is used to delay sending a packet
     * by making current thread sleep for delayTime millisecond
     */
    private static void delayPacket(){
        try {
            Thread.currentThread().sleep( delayTime );
        } catch (InterruptedException e) {
            IO.print("Cannot make current thread sleep!");
            e.printStackTrace();
        }
    }

    /**
     * This method is used to compare the block number of the target packet and err_blockNum
     * @param packet target packet
     * @return true if the block number of the target packet matches with err_blockNum
     */
    private boolean compareBlockNum( DatagramPacket packet ){
        return err_blockNum == PacketUtilities.getBlockNum( packet );
    }
}
