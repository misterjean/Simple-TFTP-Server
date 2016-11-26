package Server;
import Utilities.*;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import Utilities.TFTPErrorPacket.ErrorType;



public class Server {
    //TODO:  Set port
    private static final int SERVERPORT = Config.SERVER_LISTENING_PORT;

    private static String defaultDir = System.getProperty("user.dir") + "/storage/";
    private String publicFolder = defaultDir; // where all the file are stored/read from

    private int threadCount = 0; // number of thread
    private TFTPRequestListener tftpRequestListener;
    private boolean verbose;


    public Server() {
        new File(publicFolder).setWritable(true);
        verbose = true;
        tftpRequestListener = new TFTPRequestListener(this, SERVERPORT);
        tftpRequestListener.start();
    }

    public static void main(String [] args) {
        Server server = new Server();
        Scanner scanner = new Scanner(System.in);

        for(;;) {
            if (server.getVerbose()) {
                IO.print("Verbose is turned on...");
            } else {
                IO.print("Verbose is turned off...");
            }
            System.out.print("Server: ");
            String cmdLine = scanner.nextLine().toLowerCase();
            String[] command =  cmdLine.split("\\s+");

            if (command[0].length() == 0) {
                continue;
            }
            if (command[0].equals("help")) {
                System.out.println("Available commands:");
                IO.print("verbose: toggle verbose mode off or on");
                System.out.println(" stop: stop the server (when current transfers finish");
                System.out.println(" ls: List out the the public directory for file transfer");
                System.out.println("cd : Change the directory for file transfer. Specify path ");
                System.out.println("defaultdir : Change the directory for file transfer to default public directory.");

            } else if (command[0].equals("stop")) {
                System.out.println("Stopping server when the current transfer finish");
                server.stop();
                scanner.close();
            }else if (command[0].equals("verbose")) {
                server.toggleVerbose();
            }else if (command[0].equals("ls")){
                System.out.println("Current shared directory: " + server.getPublicFolder());

            } else if (command[0].equals("cd") && command.length > 1 && command[1].length() > 0) {
                if ( new File(command[1]).isDirectory()) {
                    if (!command[1].endsWith("/")) {
                        server.publicFolder = command[1] + File.separator;
                    }
                } else {
                    System.out.println("Invalid directory");
                }
            } else if(command[0].equals("defaultdir")) {
                server.publicFolder = defaultDir;
            } else {
                System.out.println("Invalid commands. Type the help command to get available commands: ");
            }

        }

    }

    synchronized public void incrementThreadCount() {
        threadCount++;
    }
    synchronized public void decrementThreadCount() {
        threadCount--;
        if (threadCount <= 0) {
            notifyAll();
        }
    }
    synchronized public int getThreadCount() {
        return threadCount;
    }
    public void stop() {
        tftpRequestListener.getSocket().close();
        System.out.println("Stopping... waiting for threads to finish");

        while(getThreadCount() > 0) {
            //let wait for thread to finish
            try {
                wait();
            }catch (InterruptedException ie) {
                System.out.println("Stopping was interrupted. Failed to stop properly");
                System.exit(1);
            }
        }
        System.out.println("Exiting");
        System.exit(0);
    }
    public boolean getVerbose(){return verbose;}
    public void toggleVerbose() {
        this.verbose = !this.verbose;
    }
    public String getPublicFolder() {
        return publicFolder;
    }
    public TFTPTransferHandler newTransferThread(TFTPRRQWRQPacket packet, InetAddress address, int port) {
        return new TFTPTransferHandler(this, packet, address, port);
    }

}
