package utilities;

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
    private final Scanner scanner = new Scanner( System.in );

    /**
     * just System.out.print(), in a shorter form
     * in order to save time while typing
     */
    public static void print(String string){
        System.out.println(string);
    }

    /**
     * it prompts user to input something
     * @param prompt message that will be print out when it prompts user to input
     * @return the user input (String)
     */
    public String input(String prompt){
        print( prompt + "\n" );
        return scanner.nextLine();
    }

    /**
     * to determine whether a string, especially from user input, is an integer or not
     * @param string the target string
     * @return true if the string is an integer, false otherwise
     */
    public boolean isInteger(String string){
        try{
            Integer.parseInt(string);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    /**
     * same as isInteger(String), however this one will assign the integer to another variable
     * @param string the target string
     * @param output the integer that converted from the target string
     * @return true if the string is an integer false otherwise
     */
    public boolean isInteger(String string, int output){
        try{
            output = Integer.parseInt(string);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
}
