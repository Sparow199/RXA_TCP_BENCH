package client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ThreadModeClient implements Runnable {

	private int id;
	private Socket socket;
	private String host;
	private int port;
	private OutputStream os;
	private byte[] buffer;
	private long bufferToSend;
	private int lastBufferSize;
	private long default_bytesToSend;

	public ThreadModeClient(int id, String host, int port, int bufferSize,
			long bytesToSend, long default_bytesToSend) {
		this.id = id;
		
		this.buffer = new byte[bufferSize];
		this.bufferToSend = (int) Math.ceil((double) bytesToSend
				/ (double) bufferSize);
		this.lastBufferSize = (int) ((bytesToSend % bufferSize) % Integer.MAX_VALUE); // bufferSize
																						// should
																						// be
																						// '
																						// <
																						// Integer.MAX_VALUE
																						// =
																						// 2147483647
																						// '
		this.default_bytesToSend = default_bytesToSend;
		this.host = host;
		this.port = port;
	}

	public void run() {

		long result = 0;
		while (result < default_bytesToSend) {
			try {
				try {
					this.socket = new Socket(this.host,
							this.port);
				} catch (UnknownHostException e) {
					System.err.println("Erreur de la résolution de '"
							+ host + "' : " + e.getMessage());
					System.exit(1);
				} catch (NumberFormatException e) {
					System.err.println("Erreur du parse de '" + port
							+ "' en Integer : " + e.getMessage());
					System.exit(1);
				} catch (IOException e) {
					System.err.println("Erreur du bind de la socket avec '"
							+ host + ":" + port + "' : " + e.getMessage());
					System.exit(1);
				}
				try {
					this.os = socket.getOutputStream();
				} catch (IOException e) {
					System.err.println("Thread " + this.id
							+ " : Impossible de récupérer l'OutputStream du socket : "
							+ e.getMessage());
					System.exit(1);
				}
				for (int i = 1; i <= this.bufferToSend; i++) {
					this.os.write(this.buffer);
					result += this.buffer.length;
				}
				this.os.write(this.buffer, 0, this.lastBufferSize);
				result += this.lastBufferSize;
			} catch (IOException e1) {
				System.err.println("Thread " + this.id
						+ " : Erreur lors de l'écriture sur le socket : "
						+ e1.getMessage());
				System.exit(1);
			}
			try {
				this.socket.close();
			} catch (IOException e) {
				System.err.println("Thread " + this.id
						+ " : Impossible de fermer le socket : "
						+ e.getMessage());
			}
		}
	}
}
