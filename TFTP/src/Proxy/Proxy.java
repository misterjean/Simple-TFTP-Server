package Proxy;

import proxyUtilities.Commands;
import proxyUtilities.IO;
import proxyUtilities.Proxy_PacketProcessor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Yue on 2016-09-19.
 * Core part of Proxy.Proxy
 */
public class Proxy {

    /**
     * Constructor for Proxy()
     */
    private Proxy(){
        try {
            Proxy_PacketProcessor.setServerAddress( InetAddress.getByName( "localhost") );
        } catch (UnknownHostException e) {
            IO.error("Unable to set the default server address to local host");
            e.printStackTrace();
        }

        Commands.initCommands();
        startInput();
    }

    /**
     * This method starts the UI for Proxy()
     */
    private void startInput(){
        IO.print( "TFTP Proxy.Proxy started."+"\nType 'help' to get the list of commands.\n" );
        while (true) {
            IO.print("Enter a command.");
            String inputtedCommand = IO.input(">").trim().toLowerCase();
            try {
                if( Commands.isSpecialCommand( inputtedCommand ) ) Commands.parseSpecialCommand( inputtedCommand );
                else if( !inputtedCommand.isEmpty() ) Commands.commandList.get( inputtedCommand ).runCommand();
            } catch ( NullPointerException e) {
               IO.error("The specified command was not recognized.\n");
            } catch (IOException e){
                e.printStackTrace ();
            }
        }
    }


    public static void main(String[] args) {
        new Proxy();
    }
}