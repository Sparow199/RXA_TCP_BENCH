package client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;

import utils.ByteFormat;

/*For(1Giga){
 newsock()
 for(){
 send(1024)
 }
 close()
 }
 */
public class ThreadMode implements Mode {

	private String host;
	private String port;
	private long default_bytesToSend; // 100Mo
	private long default_bytesPerCnx;
	private int default_buffer_size = 65536;
	private int default_thread_number = 30;
	private int default_numberOfPass = 10;

	public ThreadMode(String host, String port, long default_bytesToSend,
			long default_bytesPerCnx) {
		this.host = host;
		this.port = port;
		this.default_bytesToSend = default_bytesToSend;
		this.default_bytesPerCnx = default_bytesPerCnx;
	}

	public void start() {
		long startTime, endTime;
		ArrayList<ByteFormat> resultList = new ArrayList<ByteFormat>();
		ByteFormat bf;
		long results[] = new long[this.default_numberOfPass];
		long result;
		ArrayList<Thread> threadArray = new ArrayList<Thread>();
		Socket socket = null;

		System.out.println("Test en fonction du nombre de Threads du client - "
				+ Calendar.getInstance().getTime() + " :");

		for (int i = 1; i <= this.default_thread_number; i++) {
			for (int pass = 0; pass < this.default_numberOfPass; pass++) {
				result = 0;
				startTime = System.nanoTime();
				threadArray.clear();
				for (int j = 1; j <= i; j++) {
					threadArray.add(new Thread(new ThreadModeClient(j,
							this.host, Integer.parseInt(this.port),
							this.default_buffer_size, default_bytesPerCnx,
							default_bytesToSend / i)));
				}
				for (Thread t : threadArray) {
					t.start();
				}
				for (Thread t : threadArray) {
					try {
						t.join();
						result += default_bytesPerCnx;
					} catch (InterruptedException e) {
						System.err.println("Erreur lors du wait d'un thread : "
								+ e.getMessage());
						System.exit(1);
					}
				}
				endTime = System.nanoTime();
				results[pass] = (default_bytesToSend * 1000000000)
						/ (endTime - startTime);
			}
			result = 0;
			for (int pass = 0; pass < this.default_numberOfPass; pass++) {
				result += results[pass];
			}
			result /= this.default_numberOfPass;
			bf = new ByteFormat(result, i);
			System.out.println("Thread test avec un nombre de thread de " + i
					+ "  : " + bf.toString() + " par seconde - "
					+ Calendar.getInstance().getTime());
			resultList.add(bf);
		}
		this.createResultFile("Result_ThreadMode", resultList);
	}

	private void createResultFile(String filename, ArrayList<ByteFormat> bfList) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
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
			dataset.addValue(i.getBytes(), "Débit", i.getY() + "");
		}
		File file = new File(filename + ".png");
		JFreeChart chart = ChartFactory
				.createBarChart(
						"Exprerience sur le débit en fonction du nombre de thread du client",
						"nombre de thread du client", "Octets/seconde", dataset);
		// ///////////////////
		CategoryItemRenderer cat = new StatisticalBarRenderer();
		DefaultStatisticalCategoryDataset result = new DefaultStatisticalCategoryDataset();
		// //////////////////
		try {
			ChartUtilities.saveChartAsPNG(file, chart, 1024, 768);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
