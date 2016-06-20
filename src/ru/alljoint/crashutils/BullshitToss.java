package ru.alljoint.crashutils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Random;

public class BullshitToss {
	private static Random r = new Random();

	public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println("ru.alljoint.crashutils.BullshitToss <russian dict path[:charset]>"
					+ " <english dict path[:charset]> <working path> <time count>\n");
			System.out.println("Example: ru.alljoint.crashutils.BullshitToss ru.dicttionary:KOI8-R en.dictionary.zip c:/Java/mybullshit 1h");
			System.exit(1);
		}
		
		String ruDictPath = args[0];
		String ruDictCharset = Charset.defaultCharset().name();
		String enDictPath = args[1];
		String enDictCharset = Charset.defaultCharset().name();
		String wrkPath = args[2];
		String strTimeCount = args[3];
		
		if (ruDictPath.contains("%")) {
			String[] values = ruDictPath.split("%");
			ruDictPath = values[0];
			ruDictCharset = values[1];
			if (!Charset.isSupported(ruDictCharset)) {
				System.err.println(String.format("Charset \"%s\" for russian dictionary not supported", ruDictCharset));
				System.exit(2);
			}
		}
		
		if (enDictPath.contains("%")) {
			String[] values = enDictPath.split("%");
			enDictPath = values[0];
			enDictCharset = values[1];
			if (!Charset.isSupported(enDictCharset)) {
				System.err.println(String.format("Charset \"%s\" for english dictionary not supported", enDictCharset));
				System.exit(2);
			}
		}
		
		try {
			Dictionary dict = new Dictionary(new File(ruDictPath), ruDictCharset,
					new File(enDictPath), enDictCharset);
			
			int seconds = computeSeconds(strTimeCount);
			File wrkDir = new File(wrkPath);
			if (!wrkDir.exists() && !wrkDir.mkdirs()) {
				System.err.println(String.format("Can't create \"%s\" working directory", wrkDir.getAbsolutePath()));
				System.exit(3);
			}
			
			while (seconds > 0) {
				File folder = new File(wrkDir, createFileName(dict));
				if (!folder.exists() && !folder.mkdir()) {
					System.err.println(String.format("Can't create \"%s\" destination directory", wrkDir.getAbsolutePath()));
					System.exit(4);
				}

				int filesCount = 3 + r.nextInt(20);
				int flushCount;
				for (int files = 0; files < filesCount && seconds > 0; files++) {
					FileOutputStream fos = new FileOutputStream(new File(folder, createFileName(dict) + ".txt"));
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
					BufferedWriter bw = new BufferedWriter(osw, 32768);
					PrintWriter pw = new PrintWriter(bw);

					int lineCount = 10 + r.nextInt(200);
					flushCount = 1 + r.nextInt(7);
					for (int i = 0; i < lineCount && seconds > 0; i++) {
						StringBuilder sb = new StringBuilder(120);
						boolean first = true;
						while (sb.length() < 80 && seconds > 0) {
							if (!first) {
								sb.append(' ');
							} else
								first = false;
							if (r.nextInt(100) < 5)
								sb.append(dict.getRandomEnglishWord());
							else
								sb.append(dict.getRandomRussianWord());
							seconds--;
							flushCount--;
							if (flushCount <= 0) {
								flushCount = 1 + r.nextInt(20);
								pw.flush();
							}
							Thread.sleep(500 + r.nextInt(1000));
						}
						pw.println(sb.toString());
					}

					pw.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	private static int computeSeconds(String timeCount) {
		if (timeCount.endsWith("d")) {
			return Integer.parseInt(timeCount.replace("d", "")) * 24 * 60 * 60;
		} else if (timeCount.endsWith("h")) {
			return Integer.parseInt(timeCount.replace("h", "")) * 60 * 60;
		} else if (timeCount.endsWith("m")) {
			return Integer.parseInt(timeCount.replace("m", "")) * 60;
		}
		return Integer.parseInt(timeCount);
	}

	private static String createFileName(Dictionary dict) {
		boolean two = r.nextBoolean();
		StringBuilder sb = new StringBuilder();
		if (r.nextInt(100) < 5)
			sb.append(dict.getRandomEnglishWord());
		else
			sb.append(dict.getRandomRussianWord());
		if (two) {
			sb.append('-');
			if (r.nextInt(100) < 5)
				sb.append(dict.getRandomEnglishWord());
			else
				sb.append(dict.getRandomRussianWord());
		}
		return sb.toString();
	}
}
