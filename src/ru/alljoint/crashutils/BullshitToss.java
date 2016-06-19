package ru.alljoint.crashutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.ZipInputStream;

public class BullshitToss {
	private static ArrayList<String> ruDict = new ArrayList<>(952000);
	private static ArrayList<String> enDict = new ArrayList<>(159000);
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
			readDictionary(openDictStream(ruDictPath), ruDictCharset, ruDict);
			readDictionary(openDictStream(enDictPath), enDictCharset, enDict);
			
			int seconds = computeSeconds(strTimeCount);
			File wrkDir = new File(wrkPath);
			if (!wrkDir.exists() && !wrkDir.mkdirs()) {
				System.err.println(String.format("Can't create \"%s\" working directory", wrkDir.getAbsolutePath()));
				System.exit(3);
			}
			
			while (seconds > 0) {
				File folder = new File(wrkDir, createFileName());
				if (!folder.exists() && !folder.mkdir()) {
					System.err.println(String.format("Can't create \"%s\" destination directory", wrkDir.getAbsolutePath()));
					System.exit(4);
				}

				int filesCount = 3 + r.nextInt(20);
				int flushCount;
				for (int files = 0; files < filesCount && seconds > 0; files++) {
					FileOutputStream fos = new FileOutputStream(new File(folder, createFileName() + ".txt"));
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
								sb.append(enDict.get(r.nextInt(enDict.size())));
							else
								sb.append(ruDict.get(r.nextInt(ruDict.size())));
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

	private static void readDictionary(InputStream dictStream, String charset, ArrayList<String> dict) throws IOException {
		try (InputStreamReader isr = new InputStreamReader(dictStream, charset);
				BufferedReader br = new BufferedReader(isr, 32768)) {
			String line;
			while ((line = br.readLine()) != null) {
				dict.add(line);
			}
		}
	}
	
	private static InputStream openDictStream(String dictPath) throws IOException {
		InputStream is = new FileInputStream(dictPath);
		if (dictPath.endsWith(".zip")) {
			ZipInputStream zis = new ZipInputStream(is);
			zis.getNextEntry();
			return zis;
		}
		return is;
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

	private static String createFileName() {
		boolean two = r.nextBoolean();
		StringBuilder sb = new StringBuilder();
		if (r.nextInt(100) < 5)
			sb.append(enDict.get(r.nextInt(enDict.size())));
		else
			sb.append(ruDict.get(r.nextInt(ruDict.size())));
		if (two) {
			sb.append('-');
			if (r.nextInt(100) < 5)
				sb.append(enDict.get(r.nextInt(enDict.size())));
			else
				sb.append(ruDict.get(r.nextInt(ruDict.size())));
		}
		return sb.toString();
	}
}
