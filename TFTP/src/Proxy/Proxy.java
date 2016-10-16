package Proxy;

import Utilities.Commands;
import Utilities.IO;
import Utilities.Proxy_PacketProcessor;

import java.io.IOException;

/**
 * Created by Yue on 2016-09-19.
 * Core part of Proxy.Proxy
 */
public class Proxy {

    private Proxy(){
        Commands.generateCommands();

        startInput();

    }

    private void startInput(){
        IO.print( "TFTP Proxy.Proxy started."+"\nType 'help' to get the list of commands." );
        while (true) {
            String inputtedCommand = IO.input().trim().toLowerCase();
            try {
                if( !inputtedCommand.isEmpty() ) Commands.commandList.get( inputtedCommand ).runCommand();
            } catch ( NullPointerException e) {
                IO.print("The specified command was not recognized.\n");
            } catch (IOException e){
                e.printStackTrace ();
            }
        }
    }

    public static void main(String[] args) {
        new Proxy();
    }
}