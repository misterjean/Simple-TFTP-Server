package proxyUtilities;

/**
 * Created by Yue on 2016-11-04.
 * This class provides a menu for simulating errors on proxy
 */
public class Menu {
    /**
     * This method is used to print menu option
     * @param i stage
     */
    private static void printMenu(int i){
        switch( i ){
            case 0:
                IO.print( "Choose a packet type to generate error: \n" +
                          "\t1. RRQ Packet\n" +
                          "\t2. WRQ Packet\n" +
                          "\t3. DATA Packet\n" +
                          "\t4. ACK Packet" );
                break;
            case 1:
                IO.print( "Choose an error type: \n" +
                          "\t1. Lose a packet\n" +
                          "\t2. Delay a packet\n" +
                          "\t3. Duplicate a packet\n" +
                          "\t4. Illegal TFTP operation\n" +
                          "\t5. Unknown transfer ID" );
                break;
        }
    }

    /**
     * This method provides the actual functionality of menu
     */
    public static  void startMenu(){
        int stage = 0;
        int opcode = 0;
        int blockNum = 0;
        int errorCode = 0;
        int delayTime = 0;
        int illoperation = 0;
        String rawInput;
        while( stage >= 0 ){
            switch ( stage ){
                case 0:
                    printMenu( stage );
                    /*
                    Prompt user to enter an opcode
                    1-RRQ packet
                    2-WRQ packet
                    3-DATA packet
                    4-ACK packet
                     */

                     /*
                    Move forward
                     */
                    stage = 1;

                    while ( opcode < 1 || opcode > 4){
                        rawInput  = IO.input(">").trim().toLowerCase();
                        if( rawInput.equals("exit") || rawInput.equals("quit") || rawInput.equals("q") ) {
                            stage = -1;
                            break;
                        }
                        opcode = IO.string2int( rawInput );
                    }
                    /*
                    Prompt user to enter a block number for DATA or ACK packet
                     */
                    if( opcode > 2 ){
                        IO.print( "Please enter the block number: " );
                        int out1 = askForInt();
                        if ( out1 == -1 ) stage = -1;
                        else blockNum = out1;
                    }

                    break;
                case 1:
                    printMenu( stage );
                    /*
                    Prompt user to enter an error code
                    1-Lose a packet
                    2-Delay a packet
                    3-Duplicate a packet
                    4-Illegal TFTP operation
                    5-Unknown transfer ID
                     */

                    /*
                    Move to the last step
                     */
                    stage = 1000;

                    while( errorCode < 1 || errorCode > 5){
                        rawInput = IO.input(">");
                        if( rawInput.equals("exit") || rawInput.equals("quit") || rawInput.equals("q") ) {
                            stage = -1;
                            break;
                        }
                        if( IO.isInteger(rawInput) ) errorCode = Integer.parseInt( rawInput );
                    }
                    if( errorCode == 2){ //delay a packet, prompt user for a time for delay
                        IO.print( "Please enter a time(millisecond) for delay: " );
                        int out2 = askForInt();
                        if( out2 == -1 ) stage = -1;
                        else delayTime = out2;
                    }
                    if( errorCode == 4){ //illegal TFTP operation, let the user choose which error to be sim
                        int i;
                        IO.print( "Please choose a specific type error to generate: ");
                        IO.print( "1. Change packet opcode" );
                        IO.print( "2. Append more data to the packet");
                        IO.print( "3. Shrink the packet");
                        IO.print( "4. Change the port number - Invalid TID");
                        if( opcode == 1 || opcode == 2){
                            IO.print( "5. Remove the byte '0' after the file name");
                            IO.print( "6. Remove the byte '0' after the mode");
                            IO.print( "7. Modify the string mode");
                            IO.print( "8. Remove File name from the packet");
                        }
                        while( true ){
                            i = askForInt();
                            int max = 5;
                            if( opcode == 1 || opcode == 2) max = 9;
                            if( i < max ) break;
                        }
                        if( i == -1 ) stage = -1;
                        else illoperation = i;
                    }
                    break;
                case 1000:
                    IO.print( "Confirm the settings: " );
                    IO.print( "Target packet: " + PacketUtilities.getPacketName( opcode ) );
                    if( opcode > 2) IO.print( "Block number: " + blockNum );
                    IO.print( "Error Type: " + getErrorName( errorCode ) );
                    if( errorCode == 2) IO.print( "The packet will be delayed for " + delayTime + " milliseconds(" + delayTime/1000 + " seconds)." );
                    if( errorCode == 4) IO.print( "The illegal operation: " + getIllOpName( illoperation ) );
                    boolean bool = true;
                    IO.print("Type 'yes/y' to confirm the setting and generate the error, type 'no/n' otherwise.");
                    while ( bool ){
                        String str = IO.input( ">" ).trim().toLowerCase();
                        if( str.equals("yes") || str.equals("y") ){
                            Proxy_PacketProcessor.setErrMode( true );
                            Proxy_PacketProcessor.setErr_opcode( opcode );
                            Proxy_PacketProcessor.setErr_blockNum( blockNum );
                            Proxy_PacketProcessor.setErrCode( errorCode );
                            Proxy_PacketProcessor.setDelayTime( delayTime );
                            Proxy_PacketProcessor.setIlloperation( illoperation );
                            bool = false;
                        }
                        else if ( str.equals("no") || str.equals("n") || quitOrNah( str ) ) {
                            IO.print( "Setting has been aborted." );
                            bool = false;
                        }
                    }
                    /*
                    end the while loop
                     */
                    stage = -1;
                    break;
            }
        }
        //end of while loop
    }

    /**
     * This method is used to convert an error code(int), into error name(string)
     * @param errorCode error code that needs to be converted
     * @return the corresponding error name
     */
    private static String getErrorName( int errorCode ){
        switch( errorCode ){
            case 1:
                return "Lose a packet";
            case 2:
                return "Delay a packet";
            case 3:
                return "Duplicate a packet";
            case 4:
                return "Illegal TFTP operation";
            case 5:
                return  "Unknown transfer ID";
            default:
                return "This should never happen!!!!";
        }
    }

    static String getIllOpName( int i ){
        switch( i ){
            case 1:
                return "change packet opcode";
            case 2:
                return "append more data to the packet";
            case 3:
                return "shrink the packet";
            case 4:
                return "change the port number - Invalid TID";
            case 5:
                return "remove the byte '0' after the file name";
            case 6:
                return "remove the byte '0' after the mode";
            case 7:
                return "modify the string mode";
            case 8:
                return "remove File name from the packet";
            default:
                return "this should never happen";
        }
    }

    /**
     * This method is used to detected whether user wants to quit input or not
     * @param string user input
     * @return true if key word, such as 'exit', 'quit', or 'q' is detected, false otherwise
     */
    private static boolean quitOrNah( String string ){
        return string.equals("exit") || string.equals("quit") || string.equals("q");
    }

    /**
     * This method is used to prompt user to input a int value,
     * @return -1 if the user decided to quit, anything above 0 if normal
     */
    private static int askForInt() {
        String rawInput = "-2";
        while( IO.string2int( rawInput ) <= 0 ){
            rawInput = IO.input( ">" );
            if( quitOrNah( rawInput ) ) {
                return -1;
            }
        }
        return IO.string2int( rawInput );
    }
}
