package Utilities;

import Client.Client;
import Server.Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SyncFailedException;
import java.net.InetAddress;
import java.net.SocketException;

public class TFTPTransferHandler extends Thread{
	private TFTPConnection conn;
	private String fileName;
	private String filePath;
	private boolean isReadRequest;
	private Server server;
	private boolean verbose;



	public TFTPTransferHandler(Server server, TFTPRRQWRQPacket packet, InetAddress toAddress, int toPort) {
		try {
			this.server = server;
			conn = new TFTPConnection();
			conn.setRemoteAddress(toAddress);
			conn.setRemoteTid(toPort);
			this.fileName = packet.getFilename();
			verbose = server.getVerbose();
			conn.setVerbose(verbose);
			this.filePath = server.getPublicFolder() + fileName;
			this.isReadRequest = packet.isReadRequest();
		} catch (SocketException se) {
			IO.print("Failed to open socket for transfer for" + fileName);
		}
	}

	@Override
	public void run() {
		server.incrementThreadCount();

		// Dont allow file that start with a .
		if (fileName.charAt(0) == '.') {
			conn.sendAccessViolation("This server reject transfering unix hidden files( files that start with a \".\")");
		} else if (isReadRequest) {
			this.sendFileToClient();
		} else {
			this.receiveFileFromClient();
		}
		server.decrementThreadCount();
	}

	public void sendFileToClient() {

		int blockNumber = 1;

		FileInputStream fs;
		try {

			// Check that file exists
			File file = new File(filePath);
			if (!file.exists()) {
				throw new FileNotFoundException();
			}
			
			if (!file.isAbsolute()) {
				conn.sendAccessViolation("Trying to access file in private area");
				return;
			}

			fs = new FileInputStream(file);
			int bytesRead;

			// Read file in 512 byte chunks
			byte[] data = new byte[TFTPDATAPacket.MAXFILEDATALENGTH];

			do {
				bytesRead = fs.read(data);

				// Special case when file size is multiple of 512 bytes
				if (bytesRead == -1) {
					bytesRead = 0;
					data = new byte[0];
				}

				// Send data, receive ACK
				try {
					conn.sendData(blockNumber, data, bytesRead);
					conn.receiveAck(blockNumber);
				} catch (TFTPAbortException e) {
					Log.d("Aborting transfer of " + fileName + ": "
							+ e.getMessage(), verbose);
					fs.close();
					return;
				}
				blockNumber++;
			} while (bytesRead == TFTPDATAPacket.MAXFILEDATALENGTH);
			fs.close();

			IO.print("Done sending file \'" + fileName + "\' to client");
		} catch (FileNotFoundException e1) {
			Log.d("File not found: " + fileName, verbose);
			conn.sendFileNotFound("Could not find: " + fileName);
			return;
		} catch (IOException e) {
			Log.d("IOException: " + e.getMessage(), verbose);
			return;
		}
	}

	public void receiveFileFromClient() {
		try {
			// Check that file does not exist already
			File file = new File(filePath);
			if (file.exists()) {
				//@TODO Handle
				//IO.print("File already exist");
				conn.sendFileAlreadyExists(fileName + " already exists");
				return;
			}

			if (!file.isAbsolute()) {
				//@TODO cant access file due to permission
				conn.sendAccessViolation("Trying to access file in private area");
				return;
			}

			if (!file.getParentFile().canWrite()) {
				//@TODO cant write file due to permission
				conn.sendAccessViolation("Cannot write to a readonly folder");
				return;
			}
			
			FileOutputStream fs = new FileOutputStream(file);
			int blockNumber = 0;
			TFTPDATAPacket dataPk;

			do {
				try {
					//System.out.println("Block number: "+ blockNumber);
					conn.sendAck(blockNumber);
					dataPk = conn.receiveData(++blockNumber);

					if (file.canWrite()) {
						fs.write(dataPk.getFileData());
						fs.getFD().sync();
					} else {
						//@TODO cannot write to readonly file
						conn.sendAccessViolation("Cannot write to a readonly file");
						return;
					}
				} catch (TFTPAbortException e) {
					fs.close();
					file.delete();
					Log.d("Aborting transfer of " + fileName + ": "
							+ e.getMessage(), verbose);
					return;
				} catch (SyncFailedException e) {
					fs.close();
					file.delete();
					//@TODO disk error
					conn.sendDiscFull("Failed to sync with disc, likely is full");
					return;
				}
			} while (!dataPk.isLastDataPacket());

			// Send final ACK packet
			try {
				conn.sendAck(blockNumber);
			} catch (Exception e) {
				// no worries, this ACK was just a courtesy
			}

			IO.print("Done receiving file \'" + fileName + "\' from client");
			fs.close();
		} catch (FileNotFoundException e) {
			new File(filePath).delete();
			//IO.print("Cannot write to a readonly file");
			conn.sendAccessViolation("Cannot write to a readonly file");
			return;
		} catch (IOException e) {
			new File(filePath).delete();
			//IO.print("DISK full");
			IO.print("IOException with file: " + fileName);
			conn.sendDiscFull(e.getMessage());
			return;
		}
	}
}
