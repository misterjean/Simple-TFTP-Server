package Server;

import Utilities.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by Guelor on 2016-11-26.
 */
public class TFTPRequestListener extends Thread {
    protected DatagramSocket socket;
    private int boundPort;
    private Server server;

    public TFTPRequestListener (Server server, int boundPort) {
        this.server    = server;
        this.boundPort = boundPort;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        server.incrementThreadCount();

        try {
            socket = new DatagramSocket(boundPort);
        }catch (SocketException se) {
            System.out.println("Failed to bind to port: "+ boundPort);
            System.exit(1);
        }

        try {
            Log.d("Server is listening for request on port: "+ boundPort, server.getVerbose());

            while(!socket.isClosed()) {
                DatagramPacket dp = TFTPPacket.createDatagramForReceiving();
                socket.receive(dp);

                try {
                    TFTPPacket packet = TFTPPacket.createFromDatagram(dp);
                    if (packet instanceof TFTPRRQWRQPacket) {
                        Log.d("Received transfer packer, starting new Transfer thread", server.getVerbose());

                        TFTPTransferHandler tftpTransferHandler =
                                server.newTransferThread( (TFTPRRQWRQPacket) packet, dp.getAddress(), dp.getPort());

                        tftpTransferHandler.start();

                    } else {
                        // We got a valid packet but not a request
                        // We ignore the error packets, else we send error
                        if (!(packet instanceof TFTPErrorPacket)) {
                            // Send an illgeal operation, could also send Uknown TID
                            DatagramSocket errorSocket = new DatagramSocket();
                            String errMsg = "Received the wrong kind of packet on request listener.";
                            TFTPErrorPacket errorPacket =
                                    TFTPPacket.createErrorPacket(TFTPErrorPacket.ErrorType.ILLEGAL_OPERATION, errMsg);
                            dp = errorPacket.generateDatagram(dp.getAddress(),dp.getPort());

                            errorSocket.send(dp);
                            errorSocket.close();

                            Log.d("Sending illegal operation error packet with message: "+ errMsg, server.getVerbose());
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Received an invalid packet open new socket and send error
                    DatagramSocket errorSocket = new DatagramSocket();
                    Log.d("Server Received invalid request packet", server.getVerbose());
                    TFTPErrorPacket tftpErrorPacket =
                            TFTPPacket.createErrorPacket(TFTPErrorPacket.ErrorType.ILLEGAL_OPERATION, e.getMessage());
                    dp = tftpErrorPacket.generateDatagram(dp.getAddress(), dp.getPort());
                    errorSocket.send(dp);
                    errorSocket.close();
                }
            }
        } catch (IOException io) {
            //Ignore this exception socket is likely closed
            io.printStackTrace();
        }
        socket.disconnect();
        Log.d("Request listener is thread has stopped", server.getVerbose());
        server.decrementThreadCount();

    }

}
