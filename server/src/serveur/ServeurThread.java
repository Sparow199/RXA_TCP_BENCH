package serveur;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ServeurThread extends Thread {
 private Socket socket;
 private byte buffer[];
 private int default_buffer_size = 512;
 
 public ServeurThread(Socket socket){
	 this.socket = socket;
	 this.buffer = new byte[default_buffer_size];
 }
 
 public ServeurThread(Socket socket, int buffer_size){
	 this.socket = socket;
	 this.buffer = new byte[buffer_size];
 }
 
 
 
 private int readClient(){
		InputStream inputStream;
		int result = 0;
		int tempReadByte = 0;
		
		try{
			inputStream = this.socket.getInputStream();
		}catch (IOException e1) {
			System.err.println("Erreur lors de la récupération de l'inputStream du socket : "+e1.getMessage());
			return 0;
		}
		try {
			while( ( tempReadByte = inputStream.read(this.buffer)) != -1 ){
				result += tempReadByte;
			}
		} catch (IOException e) {
			System.err.println("Erreur lors de la lecture de données : "+e.getMessage());
			return 0;
		}
		try {
			this.socket.close();
		} catch (IOException e) {
			System.err.println("Erreur lors de la fermeture du socket : "+e.getMessage());
			return 0;
		}
		return result;
	}

 public void run(){
	 this.readClient();
 }
}
