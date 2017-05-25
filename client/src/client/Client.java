package client;

public class Client {
	private Mode mode;

	public Client(Mode mode) {
		this.mode = mode;
	}

	public void start() {
		if (this.mode != null) {
			this.mode.start();
		}
	}

	private static void usage() {
		System.out
				.print("Usage:\n\tjava client { -Backup <BackupFile> | <remote_host> <remote_port> [options] }\n"
						+ "\nOptions :\n\n"
						+ "\t-s <begin index of ConnectionMode Test> (default = 1 byte per connexion)\n"
						+ "\t-e <end index of ConnectionMode Test> (default = 100000 bytes per connexion)\n"
						+ "\t-b <number of bytes send in each test of ConnectionMode Test for small test (bytes/connexion smaller than sendBufferSize)>\n"
						+ "\t-B <number of bytes send in each test of ConnectionMode Test for big test (bytes/connexion bigger than sendBufferSize)>\n"
						+ "\t-buffer <sendBufferSize> (default = 1024, 0 or \"auto\" for modeAuto)\n"
						+ "\t\tmodeAuto is for set bufferSize to the same size than data to send per connection\n"
						+ "\t-chart <name of output chart> (without any extension. Default = \"Result_ConnectionMode\")\n"
						+ "\t-limit <limit between small tests and big tests> (default = 1000)\n"
						+ "\t-p <step between tests> (default = 10)\n"
						+ "\t-pass <number of passes for each test> (default = 10)\n"
						+ "\t-backup <filename of backup file> (this generate a chart from a backup file given as argument)\n"
						+ "\nBackup options:\n\n"
						+ "\t-title <chart title> (default = \"title\")\n"
						+ "\t-axisX <title of axisX>"
						+ "\t-axisY <title of axisY>" + "Exit.\n");
	}

	public static void main(String[] args) {
		int startTestConnectionMode = 0;
		int endTestConnectionMode = 100000;
		long smallBytesToSend = 1000;
		long bigBytesToSend = 1000000;
		int sendBufferSize = 1024;
		String chartTitle = "Result_ConnectionMode";
		boolean modeAuto = false;
		int limitSmallTests = 1000;
		int numberOfPasses = 10;
		int stepBetweenTests = 10;

		String host_addr = null;
		String host_port = null;

		/* BackUp options */
		String title = "title";
		String axisX = "axisX";
		String axisY = "axisY";
		
		long default_bytesPerCnx = 16384;
		long default_bytesToSend = 100000000; // 100Mo
		
		/* Which test to test (default = both) */
		boolean connectionMode = true;
		boolean threadMode = true;
		
		Client client;
		if (args.length < 2) {
			usage();
			System.exit(1);
		}
		for (int i = 0; i < args.length; i++) {
			if (i == 0) {
				host_addr = args[0];
			} else if (i == 1) {
				host_port = args[1];
			} else {
				if (args[i].equals("-s")) {
					try {
						startTestConnectionMode = Integer.parseInt(args[++i]);
						continue;
					} catch (NumberFormatException e) {
						usage();
						System.out.println("-s <Integer>");
						System.exit(1);
					}
				}
				if (args[i].equals("-e")) {
					try {
						endTestConnectionMode = Integer.parseInt(args[++i]);
						continue;
					} catch (NumberFormatException e) {
						usage();
						System.out.println("-e <Integer>");
						System.exit(1);
					}
				}
				if (args[i].equals("-b")) {
					try {
						smallBytesToSend = Integer.parseInt(args[++i]);
						default_bytesPerCnx = smallBytesToSend;
						continue;
					} catch (NumberFormatException e) {
						usage();
						System.out.println("-b <Integer>");
						System.exit(1);
					}
				}
				if (args[i].equals("-B")) {
					try {
						bigBytesToSend = Integer.parseInt(args[++i]);
						default_bytesToSend = bigBytesToSend;
						continue;
					} catch (NumberFormatException e) {
						usage();
						System.out.println("-B <Integer>");
						System.exit(1);
					}
				}
				if (args[i].equals("-buffer")) {
					if (args[i + 1].equals("0") || args[i + 1].equals("auto")) {
						modeAuto = true;
						i++;
						continue;
					} else {
						try {
							sendBufferSize = Integer.parseInt(args[++i]);
							continue;
						} catch (NumberFormatException e) {
							usage();
							System.out.println("-buffer <Integer>");
							System.exit(1);
						}
					}
				}
				if (args[i].equals("-chart")) {
					chartTitle = args[++i];
					continue;
				}
				if (args[i].equals("-limit")) {
					try {
						limitSmallTests = Integer.parseInt(args[++i]);
						continue;
					} catch (NumberFormatException e) {
						usage();
						System.out.println("-limit <Integer>");
						System.exit(1);
					}
					continue;
				}
				if (args[i].equals("-p")) {
					try {
						stepBetweenTests = Integer.parseInt(args[++i]);
						continue;
					} catch (NumberFormatException e) {
						usage();
						System.out.println("-p <Integer>");
						System.exit(1);
					}
					continue;
				}
				if (args[i].equals("-pass")) {
					try {
						numberOfPasses = Integer.parseInt(args[++i]);
						continue;
					} catch (NumberFormatException e) {
						usage();
						System.out.println("-pass <Integer>");
						System.exit(1);
					}
					continue;
				}

				/* Backup options */
				if (args[i].equals("-title")) {
					title = args[++i];
					continue;
				}
				if (args[i].equals("-axisX")) {
					axisX = args[++i];
					continue;
				}
				if (args[i].equals("-axisY")) {
					axisY = args[++i];
					continue;
				}
				if (args[i].equals("-C")) {
					connectionMode = true;
					threadMode = false;
					continue;
				}
				if (args[i].equals("-T")) {
					threadMode = true;
					connectionMode = false;
					continue;
				}
				usage();
				System.out
						.println("'" + args[i] + "' argument not understood.");
				System.exit(1);
			}
		}

		if (args[0].equals("-backup")) {
			client = new Client(new BackUpMode(args[1], title, axisX, axisY));
			client.mode.start();
			return;
		}
		if( connectionMode ){
			client = new Client(new ConnectionMode(host_addr, host_port,
					startTestConnectionMode, endTestConnectionMode,
					smallBytesToSend, bigBytesToSend, sendBufferSize, chartTitle,
					modeAuto, limitSmallTests, numberOfPasses, stepBetweenTests));
			client.start();
		}
		
		if( threadMode ){
			client = new Client(new ThreadMode(host_addr, host_port, default_bytesToSend, default_bytesPerCnx));
			client.start();	
		}
		 
		
	}

}
