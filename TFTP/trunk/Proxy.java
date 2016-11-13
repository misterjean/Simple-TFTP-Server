package Proxy;

import Utilities.Commands;
import Utilities.IO;
import java.io.IOException;

/**
 * Created by Yue on 2016-09-19.
 * Core part of Proxy.Proxy
 */
public class Proxy {

    /**
     * Constructor for Proxy()
     */
    private Proxy(){
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
                if( Commands.isSpecialCommand( inputtedCommand) ) Commands.parseSpecialCommand( inputtedCommand );
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