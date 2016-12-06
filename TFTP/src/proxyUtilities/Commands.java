package proxyUtilities;

import Proxy.Proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yue on 2016-09-22.
 * This class is used to provide a simple UI that takes some commands for proxy
 */
public class Commands {
    /**
     * This variable is used to store all the commands as key-value pairs
     * key - command
     * value - behavior of the command, normally should be a function
     */
    public static Map<String, Command> commandList = new HashMap<>();

    /**
     * This variable is used to store all the threads
     */
    private static ArrayList<Thread> threadList = new ArrayList<>();

    /**
     * This interface provides all the commands for the proxy
     */
    public interface Command{
        void runCommand() throws IOException;
    }

    /**
     * This method is used to initialize all the commands
     */
    public static void initCommands(){
        commandList.put("status", Commands::status );
        commandList.put("exit", ()->System.exit(0) );
        commandList.put("quit", commandList.get("exit"));
        commandList.put("menu", Commands::startMenu );
        commandList.put("clear", Commands::whip );
        commandList.put("help", Commands::helpingCentre );
        commandList.put("threads", Commands::threadList );
        commandList.put("test", Commands::testServerAddress );
        commandList.put("address", Commands::promptServerAddress);
        commandList.put("message", Commands::promptExtraMessage);
        commandList.put("mode", Commands::promptModifiedMode);
        commandList.put("shrink", Commands::promptDividedNum);
    }

    /**
     * This method starts a new thread of packet processor
     */
    private static void start(){
        try {
            while ( !Proxy_PacketProcessor.getServerAddress().isReachable(3000) ){
                IO.error( "Unavailable server address." );
                promptServerAddress();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e2){
            IO.error( "Null server address." );
            promptServerAddress();
        }

        Proxy_PacketProcessor packetProcessor = new Proxy_PacketProcessor();

        Thread newThread = new Thread( packetProcessor );

        threadList.add(newThread);

        newThread.start();
    }

    /**
     * This method provides the behavior for command 'status'
     */
    private static void status(){
        String packetName = PacketUtilities.getPacketName( Proxy_PacketProcessor.getErr_opcode() );
        IO.print("************************Proxy Status************************");
        IO.print(   "• Proxy receives request packets from port " + Proxy_PacketProcessor.getDefaultClientPort() +
                  "\n• Proxy forwards packets to server on port " + Proxy_PacketProcessor.getDefaultServerPort() +
                  "\n• Destination server address: " + Proxy_PacketProcessor.getServerAddress().toString() +
                  "\n• isReceiving: " + Proxy_PacketProcessor.getIsReceving() +
                "\n• Error Mode: " + (Proxy_PacketProcessor.getErrMode()?"On":"Off") );
        if( Proxy_PacketProcessor.getErrMode() ){
            IO.print( "\n• Packet Type(error): " + packetName +
                    "\n• Error Code: " + Proxy_PacketProcessor.getErrCode() );
        }
        if( Proxy_PacketProcessor.getErrCode() == 2 ) IO.print( "• Delay time: " + Proxy_PacketProcessor.getDelayTime() );
        if( Proxy_PacketProcessor.getErrCode() == 4 ) {
            IO.print( "• Illgal TFTP operation: " + Menu.getIllOpName( Proxy_PacketProcessor.getIlloperation() ) );
            if( Proxy_PacketProcessor.getIlloperation() == 2 ) IO.print( "• Message will be added: " + Proxy_PacketProcessor.getExtraData());
            if( Proxy_PacketProcessor.getIlloperation() == 3 ) IO.print( "• Packet will be shrieked by: " + Proxy_PacketProcessor.getDividedBy() + " times" );
            if( Proxy_PacketProcessor.getIlloperation() == 7 ) IO.print( "• Mode will be substitute to: " + Proxy_PacketProcessor.getModifiedMode() );
        }
        if( Proxy_PacketProcessor.getErr_opcode() > 2) IO.print("• Block number: " + Proxy_PacketProcessor.getErr_blockNum() );
        IO.print("• Number of threads currently running: " + Thread.activeCount() );
        IO.print("************************************************************");

    }

    /**
     * This method provides the behavior for command 'menu'
     */
    private static void startMenu() { Menu.startMenu(); }

    /**
     * This method provides the behavior for command 'clear'
     */
    private static void whip(){
        Proxy_PacketProcessor.setErrMode( false );
        Proxy_PacketProcessor.setErr_opcode( 0 );
        Proxy_PacketProcessor.setErr_blockNum( 0 );
        Proxy_PacketProcessor.setDelayTime( 0 );
        Proxy_PacketProcessor.setErrCode( 0 );
        Proxy_PacketProcessor.setIlloperation( 0 );
        Proxy_PacketProcessor.setDividedBy(30);
        Proxy_PacketProcessor.setExtraData("\n\tThis is just some extra byte that will be appended at the end of a packet. Nothing really special to look at");
        Proxy_PacketProcessor.setModifiedMode("modified_mode");

    }

    /**
     * This method provides the behavior for command 'help'
     */
    private static void helpingCentre(){
        IO.print("***********************************Command List**************************************");
        IO.print("1. start:  \n\t\tTo start a new thread that receives and sends packets\n\t\tUsage: start [number of threads that will be created]-optional");
        IO.print("2. menu:   \n\t\tTo start a menu that prompts users to enter error-simulating settings");
        IO.print("3. clear:  \n\t\tTo clean all the already-existing-error-simulating settings");
        IO.print("4. status: \n\t\tTo show the current status of proxy");
        IO.print("5. address:\n\t\tTo change the destination server address");
        IO.print("6. message:\n\t\tError sim option\n\t\tTo change the message that will appended at the end of a packet");
        IO.print("7. mode:   \n\t\tError sim option\n\t\tTo change the mpde that will substitute the original mode");
        IO.print("8. exit:   \n\t\tYou already know what it is");
        IO.print("*************************************************************************************");
    }

    /**
     * This method provides the behavior for command 'threads'
     */
    private static void threadList(){
        for (Thread aThread : threadList) {
            int id = Integer.parseInt(aThread.getName().split("-")[1]);
            IO.print("Packet processor #" + (id + 1) + ":" +
                    "\n    Status:       " + (aThread.isAlive() ? "Active" : "Dead") +
                    "\n    State:        " + aThread.getState() +
                    "\n    Thread group: " + aThread.getThreadGroup() );
        }
    }

    /**
     * Due to the format of special commands, it cannot be added to commandList
     * This method is used to detect whether an inputted command is a special command or not
     * format of a special command is "[command name] [number]"
     * For example: "start 5"
     * @param command an inputted command
     * @return true if it's a special command, false otherwise
     */
    public static boolean isSpecialCommand(String command){
        ArrayList<String> listOfSpecialCommands = new ArrayList<>();
        listOfSpecialCommands.add("start(.*)");
        listOfSpecialCommands.add("port(.*)");

        for( String string : listOfSpecialCommands ){
            if(command.matches(string) ) return true;
        }
        return false;
    }

    /**
     * Due to the format of special commands, it cannot be added to commandList
     * so this method is used to collaborate with isSpecialCommand() method to generate some behaviors for special commands
     * @param command a special command
     */
    public static void parseSpecialCommand( String command ){
        String[] words = command.split(" ", 3);

        switch( words[0] ){
            case "start":
                try{
                    for(int i = 0; i < Integer.parseInt(words[1]); i++){
                        start();
                    }
                } catch(NumberFormatException e){
                    IO.print("A valid \n");
                } catch(ArrayIndexOutOfBoundsException e){
                    start();
                }
                break;
            case "port": //format: [port] [server]/[client] [port]
                try{ //when nothing is missing
                    if( IO.isInteger( words[2]) ){
                        switch( words[1] ) {
                            case "server":
                            case "s":
                                Proxy_PacketProcessor.setDefaultServerPort( Integer.parseInt( words[2] ) );
                                IO.print("Now proxy will listens on port " + Integer.parseInt( words[2] ) + " to receive request packets from client.\n");
                                break;
                            case "client":
                            case "c":
                                Proxy_PacketProcessor.setDefaultClientPort( Integer.parseInt( words[2] ) );
                                IO.print("Now proxy will listens on port " + Integer.parseInt( words[2] ) + " to receive request packets from client.\n");
                                break;
                            default:
                                IO.print("The specified command was not recognized.\n");
                                break;
                        }
                    }
                    else IO.print("The specified command was not recognized.\n");
                }
                catch (ArrayIndexOutOfBoundsException e){ //when [port] is missing
                    try{
                        if(     !words[1].equals("server") &&
                                !words[1].equals("s") &&
                                !words[1].equals("client") &&
                                !words[1].equals("c")         )
                        { //when wrong command is inputted
                            IO.print("The specified command was not recognized.\n");
                        }
                        else { //normal
                            int portNumber = -1;
                            /*
                            prompt user for a port number
                            */
                            IO.print("Please input a port number: ");
                            while( portNumber < 0 || portNumber > 65536  ){
                                portNumber = IO.string2int( IO.input(">") );
                            }
                            /*
                            assigning port number
                            */
                            if( words[1].equals("server") || words[1].equals("s") ) {
                                Proxy_PacketProcessor.setDefaultServerPort( portNumber );
                                IO.print("Now proxy will forward request packets to server through port " + portNumber + ".\n" );
                            }
                            else{
                                Proxy_PacketProcessor.setDefaultClientPort( portNumber );
                                IO.print("Now proxy will listens on port " + portNumber + " to receive request packets from client.\n");
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e2 ){ //when both [server]/[server] [port] are missing
                        int option = 0;
                        int portNumber = -1;
                        /*
                        prompt user to choose either client or server
                         */
                        IO.print("Choose one of the following: " +
                                 "\n  1.Client" +
                                 "\n  2.Server" );
                        while( option != 1 && option != 2 ){
                            option = IO.string2int( IO.input(">") );
                        }
                        /*
                        prompt user for a port number
                        */
                        IO.print("Please input a port number: ");
                        while( portNumber < 0 || portNumber > 65536  ){
                            portNumber = IO.string2int( IO.input(">") );
                        }
                        /*
                        assigning port number
                        */
                        if( option == 2 ) {
                            Proxy_PacketProcessor.setDefaultServerPort( portNumber );
                            IO.print("Now proxy will forward request packets to server through port " + portNumber + ".\n" );
                        }
                        else{
                            Proxy_PacketProcessor.setDefaultClientPort( portNumber );
                            IO.print("Now proxy will listens on port " + portNumber + "to receive request packets from client.\n");
                        }
                    }
                }
                break;
            default:
                IO.print("sThe specified command was not recognized.\n");
        }
    }

    /**
     * This method is used to test if destination address is valid or not
     */
    private static void testServerAddress(){
        try {
            if( Proxy_PacketProcessor.getServerAddress().isReachable( 3000 ) ) {
                IO.print( "Valid server address: " + Proxy_PacketProcessor.getServerAddress().toString() );
            }
            else {
                IO.print( "Invalid server address.\n" );
            }
        } catch (IOException e) {
            IO.error( "Unable to reach the existing server address: " + Proxy_PacketProcessor.getServerAddress().toString() );
            e.printStackTrace();
        }
    }

    /**
     * This method is sued to prompt user for a server address
     */
    private static void promptServerAddress(){
        String address;
        IO.print( "Please enter a server address."  + "\nEnter 'quit' or 'exit' to abort operation.");
        while( true ){
            try {
                address = IO.input(">");
                if( address.equals("quit") || address.equals("exit") ) break;
                else if( address.equals("") ) continue;
                if ( InetAddress.getByName( address ).isReachable(3000) ) {
                    Proxy_PacketProcessor.setServerAddress( InetAddress.getByName( address ) );
                    IO.print("Now server address is: " + Proxy_PacketProcessor.getServerAddress() );
                    break;
                }
                else {
                    IO.error( "Unable to the address. " );
                    IO.print( "Please enter a server address." + "\nEnter 'quit' or 'exit' to abort operation." );
                }
            } catch (IOException e) {
                IO.error( "Unable to set the address." );
            }
        }
    }

    /**
     * This method is sued to prompt user for a message that will be appended at the end of a packet
     */
    private static void promptExtraMessage(){
        String message;
        IO.print("Enter a message that will be appended at the end of a packet: " + "\nEnter 'quit' or 'exit' to abort operation.");
        while( true ){
            message = IO.input( ">" );
            if( message.equals("quit") || message.equals("exit") ) break;
            else if( message.length() < 5 ) IO.print( "Message is too short. ");
            else if( message.length() > 200 ) IO.print( "Message is too long. ");
            else{
                Proxy_PacketProcessor.setExtraData( "\n\t" + message );
                break;
            }
        }
    }

    /**
     * This method is sued to prompt user for a mode that will substitute the mode in request packets
     */
    private static void promptModifiedMode(){
        String message;
        IO.print("Enter a new mode that will be substitute the mode inside request packets: " + "\nEnter 'quit' or 'exit' to abort operation.");
        while( true ){
            message = IO.input( ">" );
            if( message.equals("quit") || message.equals("exit") ) break;
            else if( message.length() < 5 ) IO.print( "Mode is too short. ");
            else if( message.length() > 100 ) IO.print( "Mode is too long. ");
            else{
                Proxy_PacketProcessor.setModifiedMode( message );
                break;
            }
        }
    }

    /**
     * This method is sued to prompt user for a number that will be divided by DEFAULT_DATA_LENGTH
     */
    private static void promptDividedNum(){
        String input;
        IO.print("Enter a number that will be divided by default data length, which is 516, to shrink the packet" + "\nEnter 'quit' or 'exit' to abort operation.");
        IO.print("Current value is: " + Proxy_PacketProcessor.getDividedBy() + ". Packet length after shrink will be " + (516/Proxy_PacketProcessor.getDividedBy()) + " bytes." );
        while( true ){
            input = IO.input( ">" );
            if( input.equals("quit") || input.equals("exit") ) break;
            else if( IO.isInteger(input) ){
                if( IO.string2int(input) == 1 ) IO.print("This does not make sense.");
                else if( IO.string2int(input) == 0 ) IO.print("Imagine that you have zero cookies and you split them evenly among zero friends. " +
                        "\nHow many cookies does each person get? See? It doesn’t make sense. " +
                        "\nAnd Cookie Monster is sad that there are no cookies, and you are sad that you have no friends." +
                        "\nNow please enter a non-zero integer.");
                else if( IO.string2int(input) < 10 ) IO.print("TIP: the number might be too small. However the number will still be used");
                if( IO.string2int(input) > 1 ) break;
            }
        }
        Proxy_PacketProcessor.setDividedBy( IO.string2int(input) );
    }
}