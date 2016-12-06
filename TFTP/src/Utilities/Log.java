package Utilities;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.util.Arrays;


/**
 * Created by Guelor on 2016-11-26.
 */
public class Log {
    static public void d(String message, boolean verbose) {
        if (verbose) {
            System.out.println("Thread #" + Thread.currentThread().getId()
                    + "        " + message);
        }
    }

    static public void printSendPacketDetails(TFTPPacket.Type t, DatagramPacket dp)
    {

        String data = null;
        try {
            data = new String(dp.getData(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.print(
                "\n**************************Sent Packet Information**************************" +
                        "\nThread #: " + Thread.currentThread().getId() +
                        "\nPacket Type: " + t +
                        "\nPacket Destination: " + dp.getAddress() +
                        "\nDestination Port: " + dp.getPort() +
                        "\nPacket Data(Byte): " + Arrays.toString(dp.getData()) +
                        "\nPacket Data(String): " + data +
                        "\nPacket Offset: " + dp.getOffset() +
                        "\nSocket Address: " + dp.getSocketAddress() +
                        "\n******************************************************************************\n");
    }

    static public void printSendPacketDetails(DatagramPacket dp)
    {

        String data = null;
        try {
            data = new String(dp.getData(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.print(
                "\n**************************Sent Packet Information**************************" +
                        "\nThread #: " + Thread.currentThread().getId() +
                        "\nPacket Destination: " + dp.getAddress() +
                        "\nDestination Port: " + dp.getPort() +
                        "\nPacket Data(Byte): " + Arrays.toString(dp.getData()) +
                        "\nPacket Data(String): " + data +
                        "\nPacket Offset: " + dp.getOffset() +
                        "\nSocket Address: " + dp.getSocketAddress() +
                        "\n******************************************************************************\n");
    }

    static public void printReceivePacketDetails(TFTPPacket.Type t, DatagramPacket dp)
    {
        String data = null;
        try {
            data = new String(dp.getData(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.print(
                "\n**************************Received Packet Information**************************" +
                        "\nThread #: " + Thread.currentThread().getId() +
                        "\nPacket Type: " + t +
                        "\nPacket Source: " + dp.getAddress() +
                        "\nSource Port: " + dp.getPort() +
                        "\nPacket Data(Byte): " + Arrays.toString(dp.getData()) +
                        "\nPacket Data(String): " + data +
                        "\nPacket Offset: " + dp.getOffset() +
                        "\n******************************************************************************\n");

    }

    static public void printReceivePacketDetails(DatagramPacket dp)
    {
        String data = null;
        try {
            data = new String(dp.getData(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.print(
                "\n**************************Received Packet Information**************************" +
                        "\nThread #: " + Thread.currentThread().getId() +
                        "\nPacket Source: " + dp.getAddress() +
                        "\nSource Port: " + dp.getPort() +
                        "\nPacket Data(Byte): " + Arrays.toString(dp.getData()) +
                        "\nPacket Data(String): " + data +
                        "\nPacket Offset: " + dp.getOffset() +
                        "\n******************************************************************************\n");

    }

}
