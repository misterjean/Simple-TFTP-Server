package Client;

import java.io.IOException;
import java.net.*;

/**
 * Created by Guelor on 2016-11-26.
 */
public class TFTPConnection {
    private DatagramSocket socket;
    private InetAddress remoteAddress;
    private int requestPort = 6800;
    private int remoteTid = -1;
    private DatagramPacket inDatagram = TFTPPacket.createDatagramForReceiving();
    private DatagramPacket resendDatagram;
    private int maxResendAttempts = 4;
    private int timeoutTime = 2000;
    private boolean verbose = true;


    public TFTPConnection() throws SocketException {
        this(new DatagramSocket());
    }

    public TFTPConnection(int bindPort) throws SocketException {
        this(new DatagramSocket(bindPort));
    }

    public TFTPConnection(DatagramSocket socket) throws SocketException {
        this.socket = socket;
        socket.setSoTimeout(timeoutTime);
        Log.d("connected on port "+ socket.getLocalPort(), verbose);
    }
    public void setRemoteAddress(InetAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public void setRemoteTid(int remoteTid) {
        this.remoteTid = remoteTid;
        Log.d("setting remote tid to: " + remoteTid, verbose);
    }

    public void sendRequest(TFTPRRQWRQPacket packet) throws IOException {
        resendDatagram =  packet.generateDatagram(remoteAddress, requestPort);
        socket.send(packet.generateDatagram(remoteAddress, requestPort));

    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setRequestPort(int requestPort) {
        this.requestPort = requestPort;
    }

    private void send(TFTPPacket packet) throws IOException {
        send(packet, false);
    }

    private void send(TFTPPacket packet, boolean cacheForResend)
            throws IOException {
        DatagramPacket dp = packet.generateDatagram(remoteAddress, remoteTid);

        if (verbose)
        Log.printSendPacketDetails(packet.getTFTPacketType(), dp);


        if (cacheForResend) {
            resendDatagram = dp;
        } else {
            resendDatagram = null;
        }
        socket.send(dp);
    }

    public void sendAck(int blockNumber) throws TFTPAbortException {
        try {
            send(TFTPPacket.createACKPAcket(blockNumber), false);
        } catch (Exception e) {
            throw new TFTPAbortException(e.getMessage());
        }
    }

    private void echoAck(int blockNumber) throws IOException {
        send(TFTPPacket.createACKPAcket(blockNumber));
        Log.d("sent: ack #" + blockNumber + " in response to duplicate data", verbose);
    }

    private void resendLastPacket() throws TFTPAbortException {
        if (resendDatagram == null) {
            return; // commented out to fix a limitation in error sim: throw new
            // TftpAbortException("Cannot resend last packet");
        }

        try {
            socket.send(resendDatagram);
            Log.d("Resending last transfer packet.", verbose);
        } catch (IOException e) {
            throw new TFTPAbortException(e.getMessage());
        }
    }

    private TFTPPacket receive() throws IOException, TFTPAbortException {
        while (true) {

            socket.receive(inDatagram);

            if (remoteTid > 0 && (inDatagram.getPort() != remoteTid
                    || !(inDatagram.getAddress()).equals(remoteAddress))) {
                Log.d("****** Received packet from invalid TID: "
                        + addressToString(inDatagram.getAddress(),
                        inDatagram.getPort()), verbose);
                //@TODO need to handle this case
                sendUnknownTidError(inDatagram.getAddress(),
                        inDatagram.getPort());
                continue;
            }

            try {
               TFTPPacket packet = TFTPPacket.createFromDatagram(inDatagram);
                if (verbose)
                    Log.printReceivePacketDetails(packet.type, inDatagram);

                return packet;
            } catch (IllegalArgumentException e) {
                sendIllegalOperationError(e.getMessage());
            }
        }
    }

    private void sendIllegalOperationError(String message)
            throws TFTPAbortException {
        try {
            TFTPErrorPacket pk = TFTPPacket.createErrorPacket(
                    TFTPErrorPacket.ErrorType.ILLEGAL_OPERATION, message);
            send(pk);
            Log.d("Sending error packet (Illegal Operation) with message: "
                    + message, verbose);
            throw new TFTPAbortException(message);
        } catch (IOException e) {
            throw new TFTPAbortException(message);
        }
    }

    private void sendUnknownTidError(InetAddress address, int port) {
        try {
            String errMsg = "Stop hacking fool!";
            TFTPErrorPacket pk = TFTPPacket.createErrorPacket(
                    TFTPErrorPacket.ErrorType.UNKOWN_TID, errMsg);
            socket.send(pk.generateDatagram(address, port));
            Log.d("*******  Sending error packet (Unknown TID) to "
                    + addressToString(address, port) + " with message: "
                    + errMsg, verbose);
        } catch (Exception e) {
            // Ignore
        }
    }

    public void sendFileNotFound(String message) {
        try {
            TFTPErrorPacket pk = TFTPPacket.createErrorPacket(
                    TFTPErrorPacket.ErrorType.FILE_NOT_FOUND, message);
            send(pk);
            Log.d("Sending error packet (File not Found) with message: "
                    + message, verbose);
        } catch (IOException e) {
            // Ignore
        }
    }

    public void sendDiscFull(String message) {
        try {
            TFTPErrorPacket pk = TFTPPacket.createErrorPacket(
                    TFTPErrorPacket.ErrorType.DISC_FULL_OR_ALLOCATION_EXCEEDED,
                    message);
            send(pk);
            Log.d("Sending error packet (Disc Full) with message: " + message, verbose);
        } catch (IOException e) {
            // Ignore
        }
    }

    public void sendAccessViolation(String message) {
        try {
            TFTPErrorPacket pk = TFTPPacket.createErrorPacket(
                    TFTPErrorPacket.ErrorType.ACCESS_VIOLATION, message);
            send(pk);
            Log.d("Sending error packet (Access Violation) with message: "
                    + message, verbose);
        } catch (IOException e) {
            // Ignore
        }
    }

    public void sendFileAlreadyExists(String message) {
        try {
            TFTPErrorPacket pk = TFTPPacket.createErrorPacket(
                    TFTPErrorPacket.ErrorType.FILE_ALREADY_EXISTS, message);
            send(pk);
            Log.d("Sending error packet (File Already Exists) with message: "
                    + message, verbose);
        } catch (IOException e) {
            // Ignore
        }
    }

    public TFTPDATAPacket receiveData(int blockNumber)
            throws TFTPAbortException {

        TFTPDATAPacket pk = (TFTPDATAPacket) receiveExpected(
                TFTPPacket.Type.DATA, blockNumber);

        // Auto-set remoteTid, for convenience
        if (remoteTid <= 0 && blockNumber == 1) {
            setRemoteTid(inDatagram.getPort());
        }

        return pk;
    }

    public TFTPACKPacket receiveAck(int blockNumber) throws TFTPAbortException {

        TFTPACKPacket pk = (TFTPACKPacket) receiveExpected(TFTPPacket.Type.ACK,
                blockNumber);

        // Auto-set remoteTid, for convenience
        if (remoteTid <= 0 && blockNumber == 0) {
            setRemoteTid(inDatagram.getPort());
        }

        return pk;
    }

    public void sendData(int blockNumber, byte[] fileData, int fileDataLength) throws TFTPAbortException {
        try {
            TFTPDATAPacket pk = TFTPPacket.createDataPacket(blockNumber,
                    fileData, fileDataLength);

            send(pk, true);
            Log.d("sent: data #" + blockNumber
                    + ((pk.isLastDataPacket()) ? " (last)" : ""), verbose);
        } catch (Exception e) {

            throw new TFTPAbortException(e.getMessage());
        }
    }

    private String addressToString(InetAddress addr, int port) {
        return addr.toString() + ":" + port;
    }

    private TFTPPacket receiveExpected(TFTPPacket.Type type, int blockNumber) throws TFTPAbortException {
        int timeouts = 0;

        try {
            while (true) {
                try {

                    TFTPPacket pk = receive();

                    if (pk.getTFTPacketType() == type) {
                        if (pk.getTFTPacketType() == TFTPPacket.Type.DATA) {
                            TFTPDATAPacket dataPk = (TFTPDATAPacket) pk;
                            if (dataPk.getBlockNumber() == blockNumber) {
                                return dataPk;
                            } else if(dataPk.getBlockNumber() < blockNumber) {
                                // We received an old data packet, so send
                                // corresponding ack
                                echoAck(dataPk.getBlockNumber());
                            } else {
                                // Received future block, this is invalid
                                sendIllegalOperationError("Received future data block number: "
                                        + dataPk.getBlockNumber());
                            }
                        } else if (pk.getTFTPacketType() == TFTPPacket.Type.ACK) {
                            TFTPACKPacket ackPk = (TFTPACKPacket) pk;
                            if (ackPk.getBlockNumber() == blockNumber) {
                                return pk;
                            } else if (ackPk.getBlockNumber() > blockNumber) {
                                sendIllegalOperationError("Received future ack block number: "
                                        + ackPk.getBlockNumber());
                            }
                        }
                    }else if (pk instanceof TFTPErrorPacket) {
                        TFTPErrorPacket errorPk = (TFTPErrorPacket) pk;
                        Log.d("Received error packet. Code: "
                                + errorPk.getCode() + ", Type: "
                                + errorPk.getErrorType().toString()
                                + ", Message: \"" + errorPk.getErrorMessage()
                                + "\"", verbose);

                        if (errorPk.shouldAbortTransfer()) {
                            Log.d("Aborting transfer", verbose);
                            throw new TFTPAbortException(
                                    errorPk.getErrorMessage());
                        } else {
                            Log.d("Continuing with transfer", verbose);
                        }
                    } else if (pk instanceof TFTPRRQWRQPacket) {
                        throw new TFTPAbortException(
                                "Received request packet within data transfer connection");
                    }
                }catch (SocketTimeoutException e) {
                    if (timeouts >= maxResendAttempts) {
                        throw new TFTPAbortException(
                                "Connection timed out. Giving up.");
                    }

                    Log.d("Waiting to receive " + type + " #" + blockNumber
                            + " timed out, trying again.", verbose);

                    timeouts++;
                    resendLastPacket();
                }
            }
        } catch (IOException e) {
            throw new TFTPAbortException(e.getMessage());
        }
    }


}
