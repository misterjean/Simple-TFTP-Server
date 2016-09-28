package Utilities;

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
		this.filePath = filePath;
		this.packetUtilities = packetUtilities;
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
				return;
			}

			if (!file.isAbsolute()) {
				//@TODO cant access file due to permission
				return;
			}

			if (!file.getParentFile().canWrite()) {
				//@TODO cant write file due to permission
				return;
			}
			
			FileOutputStream fs = new FileOutputStream(file);
			int blockNumber = 0;
			TFTPDATAPacket dataPk;

			do {
				try {
					packetUtilities.sendAck(blockNumber);
					dataPk = packetUtilities.receiveData(++blockNumber);

					if (file.canWrite()) {
						fs.write(dataPk.getFileData());
						fs.getFD().sync();
					} else {
						//@TODO cannot write to readonly file
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
			return;
		} catch (IOException e) {
			new File(filePath).delete();
			IO.print("DISK full");
			System.out.println("IOException with file: " + fileName);
			return;
		}
}


}
