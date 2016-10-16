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
        commandList.put("help", Commands::help );
    }

    private static void start(){
        Proxy_PacketProcessor packetProcessor = new Proxy_PacketProcessor();

        new Thread( packetProcessor ).start();

    }

    private static void status(){
        IO.print( "isReceiving: " + Proxy_PacketProcessor.getIsReceving() );
    }

    private static void help(){
        IO.print("<------------------------------------>");
        IO.print("help: show the help menu");
        IO.print("start: start the proxy service");
        IO.print("status: display the proxy status");
        IO.print("exit: exit the proxy");
        IO.print("<------------------------------------>");

    }
}