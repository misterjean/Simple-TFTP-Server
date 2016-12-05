package proxyUtilities;

import java.util.Scanner;

/**
 * Created by Yue on 2016-09-19.
 * This class contains a Scanner to gather user input
 * and several useful functions related to I/O
 */
public class IO {
    /**
     * the scanner used to gather user input
     */
    private static Scanner scanner = new Scanner(System.in);

    /**
     * just System.out.print(), in a shorter form
     * in order to save time while typing
     */
    public static void print(String string) {
        System.out.println(string);
    }

    /**
     * it prompts user to input something
     * @param prompt message that will be print out when it prompts user to input
     * @return the user input (String)
     */
    public static String input(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * to determine whether a string, especially from user input, is an integer or not
     * @param string the target string
     * @return true if the string is an integer, false otherwise
     */
    public static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * This method is used to convert a string to integer
     * @param string the string needs to be convert
     * @return the int value
     */
    public static int string2int( String string ){
        if( isInteger( string ) ) return Integer.parseInt( string );
        else return -1;
    }

    /**
     * This method is used to inform user the messages related to proxy-simulate-error
     * @param string error message
     */
    public static void printSimErrMsg( String string ){
        print( "\nSIMULATED ERROR MESSAGE: " + string );
    }


    public static void error(String string) { System.err.println("\nERROR: " + string); }


    public static void printProxyProcess(String string){ System.out.println("\nProxy is loading: " + string); }
}

