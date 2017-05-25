package serveur;

import java.io.IOException;
import java.net.ServerSocket;

public class Serveur {
	private ServerSocket ss;
	

	 /*private Socket socket;*/
	 /*private byte buffer[];*/
	 /*private int default_buffer_size = 512;*/
	
	public Serveur(int port) 
	{
		try
		{
			ss= new ServerSocket(port);
			ss.setReuseAddress(true);
		}catch(IOException e){
			System.err.println("Erreur création du socket serveur : "+e.getMessage());
			System.exit(1);
		}
		/*this.buffer = new byte[this.default_buffer_size];*/
		
	}
	/*
	private void readClient(){
		InputStream inputStream;
		
		try{
			inputStream = this.socket.getInputStream();
		}catch (IOException e1) {
			System.err.println("Erreur lors de la récupération de l'inputStream du socket : "+e1.getMessage());
			return;
		}
		try {
			while( inputStream.read(this.buffer) != -1 );
		} catch (IOException e) {
			System.err.println("Erreur lors de la lecture de données : "+e.getMessage());
			return;
		}
		try {
			this.socket.close();
		} catch (IOException e) {
			System.err.println("Erreur lors de la fermeture du socket : "+e.getMessage());
			return;
		}
	}*/
	
	public static void main(String[] args) {
		

		Serveur serveur=new Serveur(2000);
		
		/* Optimized for thread mode.*/
		ServeurThread serveurThread;
		
		
		while(true)
		{
			try
			{
				serveurThread=new ServeurThread(serveur.ss.accept());
			}catch(IOException e){
				System.err.println("Erreur de la creation de la socket");
				return;
			}
			serveurThread.start();
		}
		
//		while(true)
//		{
//			try
//			{
//				serveur.socket = serveur.ss.accept();
//			}catch(IOException e){
//				System.err.println("Erreur de la creation de la socket");
//				return;
//			}
//			serveur.readClient();
//		}
	}
}
