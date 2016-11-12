package Utilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yue on 2016-09-22.
 */
public class Commands {
    public static Map<String, Command> commandList = new HashMap<>();

    public interface Command{
        void runCommand() throws IOException;
    }

    public static void generateCommands(){
        commandList.put("start", Commands::start );
        commandList.put("status", Commands::status );
        commandList.put("exit", ()->System.exit(0) );
        commandList.put("menu", Commands::startMenu );
        commandList.put("clear", Commands::whip );
        commandList.put("help", Commands::helpingCentre );
    }

    private static void start(){
        Proxy_PacketProcessor packetProcessor = new Proxy_PacketProcessor();

        new Thread( packetProcessor ).start();
    }

    private static void status(){
        String packetName = PacketUtilities.getPacketName( Proxy_PacketProcessor.getErr_opcode() );
        IO.print("************************Proxy Status************************");
        IO.print( "• isReceiving: " + Proxy_PacketProcessor.getIsReceving() +
                  "\n• Error Mode: " + (Proxy_PacketProcessor.getErrMode()?"On":"Off") +
                  "\n• Packet Type(error): " + packetName +
                  "\n• Error Code: " + Proxy_PacketProcessor.getErrCode() );
        if( Proxy_PacketProcessor.getErrCode() == 2 ) IO.print( "• Delay time: " + Proxy_PacketProcessor.getDelayTime() );
        if( Proxy_PacketProcessor.getErr_opcode() > 2) IO.print("• Block number: " + Proxy_PacketProcessor.getErr_blockNum() );
        IO.print("************************************************************");

    }

    private static void startMenu() { Menu.startMenu(); }

    private static void whip(){
        Proxy_PacketProcessor.setErrMode( false );
        Proxy_PacketProcessor.setErr_opcode( 0 );
        Proxy_PacketProcessor.setErr_blockNum( 0 );
        Proxy_PacketProcessor.setDelayTime( 0 );
        Proxy_PacketProcessor.setErrCode( 0 );
    }

    private static void helpingCentre(){
        IO.print("***********************************Command List**************************************");
        IO.print("1. start: \n\t\tTo start a new thread that receives and sends packets");
        IO.print("2. menu:  \n\t\tTo start a menu that prompts users to enter error-simulating settings");
        IO.print("3. clear: \n\t\tTo clean all the already-existing-error-simulating settings");
        IO.print("4. status:\n\t\tTo show the current status of proxy");
        IO.print("5. exit:  \n\t\tYou already know what it is");
        IO.print("*************************************************************************************");
    }
}