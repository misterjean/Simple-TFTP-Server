package Utilities;

import java.io.IOException;
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
        commandList.put("menu", Commands::startMenu );
        commandList.put("clear", Commands::whip );
        commandList.put("help", Commands::helpingCentre );
        commandList.put("threads", Commands::threadList );
    }

    /**
     * This method starts a new thread of packet processor
     */
    private static void start(){
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
                  "\n• isReceiving: " + Proxy_PacketProcessor.getIsReceving() +
                "\n• Error Mode: " + (Proxy_PacketProcessor.getErrMode()?"On":"Off") );
        if( Proxy_PacketProcessor.getErrMode() ){
            IO.print( "\n• Packet Type(error): " + packetName +
                    "\n• Error Code: " + Proxy_PacketProcessor.getErrCode() );
        }
        if( Proxy_PacketProcessor.getErrCode() == 2 ) IO.print( "• Delay time: " + Proxy_PacketProcessor.getDelayTime() );
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
    }

    /**
     * This method provides the behavior for command 'help'
     */
    private static void helpingCentre(){
        IO.print("***********************************Command List**************************************");
        IO.print("1. start: \n\t\tTo start a new thread that receives and sends packets");
        IO.print("2. menu:  \n\t\tTo start a menu that prompts users to enter error-simulating settings");
        IO.print("3. clear: \n\t\tTo clean all the already-existing-error-simulating settings");
        IO.print("4. status:\n\t\tTo show the current status of proxy");
        IO.print("5. exit:  \n\t\tYou already know what it is");
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
        String[] words = command.split(" ", 2);

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
            default:
                IO.print("The specified command was not recognized.\n");
        }
    }

}