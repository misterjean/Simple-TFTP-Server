package Utilities;

/**
 * Created by Yue on 2016-11-04.
 */
public class Menu {

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
                          "\t3. Duplicate a packet" );
                break;
        }
    }

    public static  void startMenu(){
        int stage = 0;
        int opcode = 0;
        int blockNum = 0;
        int errorCode = 0;
        int delayTime = 0;
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
                        while( blockNum <= 0 ) {
                            rawInput = IO.input( ">" );
                            if( rawInput.equals("exit") || rawInput.equals("quit") || rawInput.equals("q") ) {
                                stage = -1;
                                break;
                            }
                            blockNum = IO.string2int( rawInput );
                        }
                    }

                    break;
                case 1:
                    printMenu( stage );
                    /*
                    Prompt user to enter an error code
                    1-Lose a packet
                    2-Delay a packet
                    3-Duplicate a packet
                     */

                    /*
                    Move to the last step
                     */
                    stage = 1000;

                    while( errorCode < 1 || errorCode > 3){
                        rawInput = IO.input(">");
                        if( rawInput.equals("exit") || rawInput.equals("quit") || rawInput.equals("q") ) {
                            stage = -1;
                            break;
                        }
                        if( IO.isInteger(rawInput) ) errorCode = Integer.parseInt( rawInput );
                    }
                    if( errorCode == 2){ //delay a packet, prompt user for a time for delay
                        IO.print( "Please enter a time(millisecond) for delay: " );
                        while( delayTime <= 0 ){
                            rawInput = IO.input( ">" );
                            if( rawInput.equals("exit") || rawInput.equals("quit") || rawInput.equals("q") ) {
                                stage = -1;
                                break;
                            }
                            delayTime = IO.string2int( rawInput );
                        }
                    }
                    break;
                case 1000:
                    IO.print( "Confirm the settings: " );
                    IO.print( "Target packet: " + PacketUtilities.getPacketName( opcode ) );
                    if( opcode > 2) IO.print( "Block number: " + blockNum );
                    IO.print( "Error Type: " + getErrorName( errorCode ) );
                    if( errorCode == 2) IO.print( "The packet will be delayed for " + delayTime + " milliseconds(" + delayTime/1000 + " seconds)." );
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
                            bool = false;
                        }
                        else if ( str.equals("no") || str.equals("n") ) {
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

    private static String getErrorName( int errorCode ){
        switch( errorCode ){
            case 1:
                return "Lose a packet";
            case 2:
                return "Delay a packet";
            case 3:
                return "Duplicate a packet";
            default:
                return "This should never happen!!!!";
        }
    }

}
