package client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import utils.ByteFormat;

public class ConnectionMode implements Mode {

	private int startTestPerCnx;
	// Limite max du test ==> de 1
	// octet par connection à
	// MAX octets par
	// connection.
	private int endTestPerCnx = 100000;

	// 1Moctet à envoyé pour chaque
	// test si le nombre d'octet par connection est suffisament important()
	// Sinon 1Koctet suffit
	private long smallBytesToSend;
	private long bigBytesToSend;
	private long bytesToSend;

	// taille du buffer d'écriture; Si le test est inférieur, on envoie une
	// portion du buffer à chaque connection, sinon on l'envoi entièrement et
	// plusieurs fois si nécessaire.
	private int sendBufferSize = 1024;

	// Buffer utilisé pour envoyer les données au serveur. Le tableau est par
	// défaut alloué mais les données ne sont pas initialisées car pas
	// pertinentes( les données reçu par le serveur sont immédiatement 'jetées'
	// ).
	private byte[] sendBuffer;

	// Nom d'hote du serveur (ou addresse IP).
	private String host;

	// Port du serveur.
	private String port;

	// Nombre de passage pour un test.
	// La moyenne sur ses n passages est ensuite calculée et retournée comme
	// résultat.
	private int numberOfPasses = 10;

	// Pas entre chaque tests.
	private int stepBetweenTests = 10;

	private String chartTitle = "Result_ConnectionMode";
	private boolean modeAuto;
	private int limitSmallTests;

	public ConnectionMode(String host, String port, int startTest, int endTest,
			long smallBytesToSend, long bigBytesToSend, int sendBufferSize,
			String chartTitle, boolean modeAuto, int limitSmallTests,
			int numberOfPasses, int stepBetweenTests) {
		this.host = host;
		this.port = port;
		this.startTestPerCnx = startTest;
		this.endTestPerCnx = endTest;
		this.smallBytesToSend = smallBytesToSend;
		this.bigBytesToSend = bigBytesToSend;
		this.sendBufferSize = sendBufferSize;
		this.chartTitle = chartTitle;
		this.modeAuto = modeAuto;
		this.limitSmallTests = limitSmallTests;
		this.numberOfPasses = numberOfPasses;
		this.stepBetweenTests = stepBetweenTests;
	}

	public void start() {
		long startTime = 0, endTime = 0, totalBytesSend = 0;
		ArrayList<ByteFormat> resultList = new ArrayList<ByteFormat>();
		InetSocketAddress inetAddress = new InetSocketAddress(this.host,
				Integer.parseInt(this.port));
		ByteFormat bf = null;
		Socket socket = null;
		OutputStream os = null;

		System.out
				.println("Test en fonction du nombre d'octet par connection - "
						+ Calendar.getInstance().getTime() + " :");

		// Pour chaque test : 'i' le nombre d'octets à envoyé par connection.
		for (int i = this.startTestPerCnx; i <= endTestPerCnx; i += this.stepBetweenTests) {

			// Cas particulier pour le test i == 0 => on effectue le test pour i
			// == 1.
			if (i == 0)
				i = 1;
			if (this.modeAuto) {
				// mode buffer auto
				// bufferSize est égal à la taille des données à envoyer par
				// connexion donc i
				this.sendBufferSize = i;

			}
			this.sendBuffer = new byte[this.sendBufferSize];
			if (i < this.limitSmallTests) {
				this.bytesToSend = this.smallBytesToSend;
			} else {
				this.bytesToSend = this.bigBytesToSend;
			}

			// Le resultat de chacun des passages stocké dans 'resultPass' (en
			// octets par seconde).
			long[] resultPass = new long[this.numberOfPasses];
			for (int pass = 0; pass < this.numberOfPasses; pass++) {

				// Nombre d'octets envoyé pour ce passage.
				totalBytesSend = 0;

				// Si 'i' est inférieur à la taille du buffer d'écriture de la
				// socket, alors on créé un nouveau buffer ayant une taille de
				// 'i'. Ceci dans un soucis de perfomance. Une alternative
				// serait utiliser la méthode write(bytes[], offset, lenght).
				// Mais cette dernière est moins performante.

				// La nouvelle taille de buffer ici est donc 'i'.

				// Début du test.
				// 'startTime' en millisecondes.
				startTime = System.nanoTime();

				// Tant que le nombre d'octets envoyés durant ce passage
				// n'est pas suffisant (valeur fixé par
				// this.default_bytesToSend).
				retry: while (totalBytesSend < this.bytesToSend) {

					// Ouverture de la connection avec le serveur.
					try {

						socket = new Socket();

						// TCP_NODELAY permet d'activer ou de désactiver
						// l'algorithme de Nagle. Ce dernier dit ceci: Si un
						// segment TCP est en attente d'acquittement (ACK),
						// et que la taille des données dans le buffer
						// d'écriture de la socket est inférieur au MTU de
						// la liaison physique, attendre que d'autres
						// données s'ajoute au buffer d'écriture. Lorsque la
						// taille des données dans le buffer atteint le MTU,
						// envoyer les données. Cette algorithme a pour but
						// d'éviter l'envoi de segment TCP comportant peu de
						// données bruts. Par ailleurs, si aucun segment
						// n'est en attente d'acquitement, envoyer les
						// données.

						// socket.setTcpNoDelay(true);
						socket.setReuseAddress(true);
						socket.connect(inetAddress, 5000);

					} catch (SocketTimeoutException e) {
						System.err
								.println("Timeout atteint lors de la connection avec '"
										+ inetAddress.getHostString()
										+ "' : "
										+ e.getMessage());
						break retry;
					} catch (UnknownHostException e) {
						System.err.println("Erreur de la résolution de '"
								+ inetAddress.getHostString() + "' : "
								+ e.getMessage());
						break retry;
					} catch (NumberFormatException e) {
						System.err.println("Erreur du parse de '"
								+ inetAddress.getPort() + "' en Integer : "
								+ e.getMessage());
						break retry;
					} catch (IOException e) {
						System.err.println("Erreur du bind de la socket avec '"
								+ inetAddress.getHostString() + ":" + port
								+ "' : " + e.getMessage());
						e.printStackTrace();
						break retry;
					}

					// Récupération de l'OuputStream permettant d'écrire
					// dans le buffer d'envoi de la socket.
					try {
						os = socket.getOutputStream();
					} catch (IOException e) {
						System.err
								.println("Erreur lors de la récupération de l'OutputStream du socket : "
										+ e.getMessage());
						break retry;
					}

					int byteSend = 0;

					while (byteSend + this.sendBufferSize <= i
							&& totalBytesSend + this.sendBufferSize <= this.bytesToSend) {
						try {
							os.write(this.sendBuffer);
							byteSend += this.sendBuffer.length;
							totalBytesSend += this.sendBuffer.length;
						} catch (IOException e) {
							System.err
									.println("Erreur lors de l'écriture sur le socket : "
											+ e.getMessage());
							break retry;
						}
					}

					if (byteSend < i) {
						if (totalBytesSend + (i - byteSend) < this.bytesToSend) {
							try {
								os.write(this.sendBuffer, 0, i - byteSend);
								totalBytesSend += i - byteSend;
								byteSend += i - byteSend;
							} catch (IOException e) {
								System.err
										.println("Erreur lors de l'écriture sur le socket du dernier bloc de données : "
												+ e.getMessage());
								break retry;
							}
						} else {
							try {
								os.write(
										this.sendBuffer,
										0,
										(int) (this.bytesToSend - totalBytesSend));
								byteSend += this.bytesToSend - totalBytesSend;
								totalBytesSend += this.bytesToSend
										- totalBytesSend;
							} catch (IOException e) {
								System.err
										.println("Erreur lors de l'écriture sur le socket du dernier bloc de données : "
												+ e.getMessage());
								break retry;
							}
						}
					}

					assert ((byteSend == i || totalBytesSend == this.bytesToSend)
							&& byteSend <= i && totalBytesSend <= this.bytesToSend);

					// Fermeture de la connection.
					try {
						os.close();
					} catch (IOException e) {
						System.err
								.println("Erreur lors de la fermeture du socket : "
										+ e.getMessage());
						break retry;
					}
				}

				// Nombre d'octet total envoyé. Fin du test.
				// 'endTime' en millisecondes.
				endTime = System.nanoTime();
				resultPass[pass] = (totalBytesSend * 1000000000)
						/ (endTime - startTime);
				/*
				 * System.out.println(i + " bytes/cnx, Pass " + (pass + 1) + "/"
				 * + this.default_numberOfPass + " : " + resultPass[pass] +
				 * " bytes/sec");
				 */
			}
			// getAverageResult retourne la moyenne sur l'ensemble des passage
			// en supprimant les plus éloignés (garde 80% des passages).
			bf = new ByteFormat(getResultAverage(resultPass), i);
			resultList.add(bf);
			System.out.println(i + " octets par connection : "
					+ String.format("%4s", bf.toString())
					+ " par seconde - "
					+ Calendar.getInstance().getTime() + ".");
			if (i == 1)
				i = 0;
		}
		this.createResultFile(this.chartTitle, resultList);
		System.out
				.println("Test en fonction du nombre d'octet par connection terminé - "
						+ Calendar.getInstance().getTime() + ".");

	}

	private long getResultAverage(long[] resultPass) {
		int purcent = 80;
		long averageResult;
		int countPass;

		boolean[] results = new boolean[this.numberOfPasses];
		for (int i = 0; i < results.length; i++) {
			results[i] = true;
		}

		for (int j = 0; j < this.numberOfPasses * purcent / 100; j++) {
			averageResult = 0;
			long tempPassToDelete = 0;
			int indexPassToDelete = 0;
			countPass = 0;
			for (int i = 0; i < resultPass.length; i++) {
				if (results[i]) {
					averageResult += resultPass[i];
					countPass++;
				}
			}
			averageResult = averageResult / countPass++;
			for (int i = 0; i < resultPass.length; i++) {
				if (results[i]
						&& (resultPass[i] - averageResult)
								* (resultPass[i] - averageResult) > tempPassToDelete) {
					tempPassToDelete = (resultPass[i] - averageResult)
							* (resultPass[i] - averageResult);
					indexPassToDelete = i;
				}
			}
			results[indexPassToDelete] = false;
		}

		averageResult = 0;
		countPass = 0;
		for (int i = 0; i < resultPass.length; i++) {
			if (results[i]) {
				averageResult += resultPass[i];
				countPass++;
			}
		}
		return (averageResult / countPass);

	}

	private void createResultFile(String filename, ArrayList<ByteFormat> bfList) {
		XYSeries xySeries = new XYSeries("Débit");
		XYSeriesCollection seriesCollection = new XYSeriesCollection();
		seriesCollection.addSeries(xySeries);
		File backUp = new File(filename + ".txt");
		ObjectOutputStream oos = null;
		try {
			backUp.createNewFile();
			oos = new ObjectOutputStream(new FileOutputStream(backUp));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			oos.writeObject(bfList);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (ByteFormat i : bfList) {
			xySeries.add(i.getY(), i.getBytes());
		}
		File file = new File(filename + ".png");
		JFreeChart chart = ChartFactory.createXYLineChart(filename,
				"Octet(s) par connection", "Octet(s) par seconde",
				seriesCollection);
		try {
			ChartUtilities.saveChartAsPNG(file, chart, 1024, 768);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
