package Utilities;

/**
 * Created by Guelor on 2016-11-26.
 */
public class Log {
    static public void d(String message, boolean verbose) {
        if ( verbose) {
            System.out.println("Thread #" + Thread.currentThread().getId()
                    + "        " + message);
        }
    }
}
