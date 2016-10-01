package Client;

import Utilities.*;

import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.net.*;

import Utilities.IO;

import static Utilities.PacketUtilities.DEFAULT_PORT;

public class Client {
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendReceiveSocket;


    Scanner in = new Scanner(System.in);
    private PacketUtilities packetUtilities;
    private TFTPTransferHandler transferHandler;

    public Client() {
        try {
            sendReceiveSocket = new DatagramSocket(DEFAULT_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        packetUtilities = new PacketUtilities(sendReceiveSocket);


        /*try {
            // Construct a datagram socket and bind it to any available
            // port on the local host machine. This socket will be used to
            // send and receive UDP Datagram packets.
            sendReceiveSocket = new DatagramSocket();
        } catch (SocketException se) {   // Can't create the socket.
            se.printStackTrace();
            System.exit(1);
        }*/
    }

    public void start()
    {
        int choice = 0;

        System.out.println("<------->");
        System.out.println("1. Write");
        System.out.println("2. Read");
        System.out.println("3. Quit");
        System.out.println("<------->");

        while (choice < 1 || choice > 3) {
            choice = Integer.parseInt(in.next());
        }

        if (choice == 1){
            writeFile();
        } else if (choice == 2) {
            //readFile();
        } else if (choice == 3){

        }
    }

    public void writeFile()
    {

        String fn = IO.input("Please enter the file that you wish to send (full path if its not within the root folder) >");
        /*File file = new File(fn);

        if (file.isDirectory()) {
            System.out.println("You cannot send a directory.");
            return;
        }

        if (!file.exists()) {
            System.out.println("The specified file does not exist.");
            return;
        }

        if (!file.canRead()) {
            System.out.println("You do not have permission to read the specified file.");
            return;
        }

        if (file.length() < 1) {
            System.out.println("The specified file appears to be empty.");
            return;
        }

        if (file.length() > IO.MAX_FILE_SIZE) {
            System.out.println("File transfer size exceeded.\nFiles must be no larger than 33553920 bytes.");
            return;
        }

        //String dp = IO.input("Please enter the full path to copy >");
*/
        long start = System.currentTimeMillis();

        this.transferHandler = new TFTPTransferHandler(fn, packetUtilities);
        transferHandler.sendFileToClient();

       /* byte[] file = new byte[100], // message we send
                fn, // filename as an array of bytes
                md, // mode as an array of bytes
                data; // reply as array of bytes
        String filename, mode; // filename and mode as Strings
        int j, len, sendPort;


        Mode run = Mode.NORMAL; // change to NORMAL to send directly to server

        if (run==Mode.NORMAL)
            sendPort = 3001;
        else
            sendPort = 23;

        // sends 10 packets -- 5 reads, 5 writes, 1 invalid
        for(int i=1; i<=11; i++) {

            System.out.println("Client: creating packet " + i + ".");

            // Prepare a DatagramPacket and send it via sendReceiveSocket
            // to sendPort on the destination host (also on this machine).

            // if i even (2,4,6,8,10), it's a read; otherwise a write
            // (1,3,5,7,9) opcode for read is 01, and for write 02
            // And #11 is invalid (opcode 07 here -- could be anything)



            // next we have a file name -- let's just pick one
            filename = "test.txt";
            // convert to bytes
            fn = filename.getBytes();

            // and copy into the msg
            System.arraycopy(fn,0,msg,2,fn.length);
            // format is: source array, source index, dest array,
            // dest index, # array elements to copy
            // i.e. copy fn from 0 to fn.length to msg, starting at
            // index 2

            // now add a 0 byte
            msg[fn.length+2] = 0;

            // now add "octet" (or "netascii")
            mode = "octet";
            // convert to bytes
            md = mode.getBytes();

            // and copy into the msg
            System.arraycopy(md,0,msg,fn.length+3,md.length);

            len = fn.length+md.length+4; // length of the message
            // length of filename + length of mode + opcode (2) + two 0s (2)
            // second 0 to be added next:

            // end with another 0 byte
            msg[len-1] = 0;

            // Construct a datagram packet that is to be sent to a specified port
            // on a specified host.
            // The arguments are:
            //  msg - the message contained in the packet (the byte array)
            //  the length we care about - k+1
            //  InetAddress.getLocalHost() - the Internet address of the
            //     destination host.
            //     In this example, we want the destination to be the same as
            //     the source (i.e., we want to run the client and server on the
            //     same computer). InetAddress.getLocalHost() returns the Internet
            //     address of the local host.
            //  69 - the destination port number on the destination host.
            try {
                sendPacket = new DatagramPacket(msg, len,
                        InetAddress.getLocalHost(), sendPort);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.exit(1);
            }

            System.out.println("Client: sending packet " + i + ".");
            System.out.println("To host: " + sendPacket.getAddress());
            System.out.println("Destination host port: " + sendPacket.getPort());
            len = sendPacket.getLength();
            System.out.println("Length: " + len);
            System.out.println("Containing: ");
            for (j=0;j<len;j++) {
                System.out.println("byte " + j + " " + msg[j]);
            }

            // Form a String from the byte array, and print the string.
            String sending = new String(msg,0,len);
            System.out.println(sending);

            // Send the datagram packet to the server via the send/receive socket.

            try {
                sendReceiveSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            System.out.println("Client: Packet sent.");

            // Construct a DatagramPacket for receiving packets up
            // to 100 bytes long (the length of the byte array).

            data = new byte[100];
            receivePacket = new DatagramPacket(data, data.length);

            System.out.println("Client: Waiting for packet.");
            try {
                // Block until a datagram is received via sendReceiveSocket.
                sendReceiveSocket.receive(receivePacket);
            } catch(IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            // Process the received datagram.
            System.out.println("Client: Packet received:");
            System.out.println("From host: " + receivePacket.getAddress());
            System.out.println("Host port: " + receivePacket.getPort());
            len = receivePacket.getLength();
            System.out.println("Length: " + len);
            System.out.println("Containing: ");
            for (j=0;j<len;j++) {
                System.out.println("byte " + j + " " + data[j]);
            }

            System.out.println();

        } // end of loop

        // We're finished, so close the socket.
        sendReceiveSocket.close();*/
    }

    public static void main(String args[])
    {

        Client c = new Client();

        c.start();
    }
}
