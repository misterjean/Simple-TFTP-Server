package Utilities;

import Client.Client;
import Server.Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SyncFailedException;

public class TFTPTransferHandler {
	private String filePath;
	private String fileName;
	private PacketUtilities packetUtilities;


	
	public TFTPTransferHandler(String fileName, String filePath, PacketUtilities packetUtilities) {
		this.fileName = fileName;
		this.filePath = filePath;
		this.packetUtilities = packetUtilities;
	}

	public TFTPTransferHandler(String fn, PacketUtilities pu) {
		this.filePath = fn;
		this.packetUtilities = pu;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
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
				packetUtilities.sendAccessViolation("Trying to access file in private area");
				return;
			}

			fs = new FileInputStream(file);
			int bytesRead;

			// Read file in 512 byte chunks
			byte[] data = new byte[TFTPDATAPacket.MAXFILEDATALENGTH];

			do {
				bytesRead = fs.read(data);
				System.out.println("Block number: "+ blockNumber);
				// Special case when file size is multiple of 512 bytes
				if (bytesRead == -1) {
					bytesRead = 0;
					data = new byte[0];
				}

				// Send data, receive ACK
				try {
					packetUtilities.sendData(blockNumber, data, bytesRead);
					packetUtilities.receiveAck(blockNumber);
				} catch (TFTPAbortException e) {
					IO.print("Aborting transfer of " + fileName + ": "
							+ e.getMessage());
					fs.close();
					return;
				}
				blockNumber++;
			} while (bytesRead == TFTPDATAPacket.MAXFILEDATALENGTH);
			fs.close();

			IO.print("Done sending file \'" + fileName + "\' to client");
		} catch (FileNotFoundException e1) {
			IO.print("File not found: " + fileName);
			packetUtilities.sendFileNotFound("Could not find: " + fileName);
			return;
		} catch (IOException e) {
			IO.print("IOException: " + e.getMessage());
			return;
		}
	}

	public void receiveFileFromClient() {
		try {
			// Check that file does not exist already
			File file = new File(filePath);
			if (file.exists()) {
				//@TODO Handle
				IO.print("File already exist");
				packetUtilities.sendFileAlreadyExists(fileName + " already exists");
				return;
			}

			if (!file.isAbsolute()) {
				//@TODO cant access file due to permission
				packetUtilities.sendAccessViolation("Trying to access file in private area");
				return;
			}

			if (!file.getParentFile().canWrite()) {
				//@TODO cant write file due to permission
				packetUtilities.sendAccessViolation("Cannot write to a readonly folder");
				return;
			}
			
			FileOutputStream fs = new FileOutputStream(file);
			int blockNumber = 0;
			TFTPDATAPacket dataPk;

			do {
				try {
					System.out.println("Block number: "+ blockNumber);
					packetUtilities.sendAck(blockNumber);
					dataPk = packetUtilities.receiveData(++blockNumber);

					if (file.canWrite()) {
						fs.write(dataPk.getFileData());
						fs.getFD().sync();
					} else {
						//@TODO cannot write to readonly file
						packetUtilities.sendAccessViolation("Cannot write to a readonly file");
						return;
					}
				} catch (TFTPAbortException e) {
					fs.close();
					file.delete();
					IO.print("Aborting transfer of " + fileName + ": "
							+ e.getMessage());
					return;
				} catch (SyncFailedException e) {
					fs.close();
					file.delete();
					//@TODO disk error
					packetUtilities.sendDiscFull("Failed to sync with disc, likely is full");
					return;
				}
			} while (!dataPk.isLastDataPacket());

			// Send final ACK packet
			try {
				packetUtilities.sendAck(blockNumber);
			} catch (Exception e) {
				// no worries, this ACK was just a courtesy
			}

			IO.print("Done receiving file \'" + fileName + "\' from client");
			fs.close();
		} catch (FileNotFoundException e) {
			new File(filePath).delete();
			IO.print("Cannot write to a readonly file");
			packetUtilities.sendAccessViolation("Cannot write to a readonly file");
			return;
		} catch (IOException e) {
			new File(filePath).delete();
			IO.print("DISK full");
			IO.print("IOException with file: " + fileName);
			packetUtilities.sendDiscFull(e.getMessage());
			return;
		}
	}
	
	public void sendFileToServer() {
		try {
			// Check that file exists
			File file = new File(filePath);
			if (!file.exists()) {
				System.out.println("Cannot find file: " + fileName);
				return;
			}

			// Check read permissions
			if (!file.canRead()) {
				System.out.println("Cannot read file: " + fileName);
				return;
			}

			// Open input stream
			FileInputStream fs = new FileInputStream(file);

			// Send request
			TFTPRRQWRQPacket reqPacket = TFTPPacket.createWriteRequestPacket(fileName,
					TFTPRRQWRQPacket.Mode.OCTET);
			this.packetUtilities.sendRequest(reqPacket);

			int blockNumber = 0;
			byte[] data = new byte[512];
			int bytesRead = 0;

			do {
                //this.packetUtilities.printPacketDetails(2);
				System.out.println("Block number: "+ blockNumber);
				this.packetUtilities.receiveAck(blockNumber);
				blockNumber++;

				bytesRead = fs.read(data);

				// Special case when file size is multiple of 512 bytes
				if (bytesRead == -1) {
					bytesRead = 0;
					data = new byte[0];
				}

				this.packetUtilities.sendData(blockNumber, data, bytesRead);
			} while (bytesRead == TFTPDATAPacket.MAXFILEDATALENGTH);

			// Wait for final ACK packet
			this.packetUtilities.receiveAck(blockNumber);

            IO.print("Successfully sent '" + fileName + "' to server.");
			fs.close();
		} catch (TFTPAbortException e) {
			IO.print("Failed to send " + fileName + ": " + "\""+ e.getMessage() + "\"");
		} catch (IOException e) {
			IO.print("IOException: failed to send " + fileName + ": "+ "\"" + e.getMessage() + "\"");
		}
	}
	
	public void receiveFileFromServer() {
		try {
			// Check write permissions
			File file = new File(filePath);
			if (file.exists() && !file.canWrite()) {
				System.out.println("Cannot overwrite file: " + fileName);
				return;
			}

			FileOutputStream fs = new FileOutputStream(filePath);

			TFTPRRQWRQPacket reqPacket = TFTPPacket.createReadRequestPacket(fileName,
					TFTPRRQWRQPacket.Mode.OCTET);

			this.packetUtilities.sendRequest(reqPacket);

			TFTPDATAPacket pk;

			int blockNumber = 1;

			do {
				System.out.println("Block number: "+ blockNumber);


				pk = this.packetUtilities.receiveData(blockNumber);

				try {

					fs.write(pk.getFileData());
					fs.getFD().sync();
				} catch (SyncFailedException e) {
					file.delete();
					fs.close();
					IO.print("Failed to sync with disc, might be full");
					packetUtilities.sendDiscFull("Failed to sync with disc, likely is full");
					return;
				}
				this.packetUtilities.sendAck(blockNumber);
				blockNumber++;
			} while (!pk.isLastDataPacket());
			IO.print("Wrote file to " +filePath);
            IO.print("Successfully received " + fileName + " from server.");
			fs.close();

		} catch (TFTPAbortException e) {
			new File(filePath).delete();
			IO.print("Failed to get " + fileName + ": " + "\""
					+ e.getMessage() + "\"");
		} catch (IOException e) {
			new File(filePath).delete();
			IO.print("IOException: failed to get " + fileName + ": " + "\"" + e.getMessage() + "\"");
		}
	}


}
