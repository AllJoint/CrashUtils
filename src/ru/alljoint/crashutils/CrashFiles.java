package ru.alljoint.crashutils;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Random;

/**
 * 
 * @author Алексей Курган
 * 
 * Разхуяриватель
 *
 */
public class CrashFiles {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("The folder path must be specified");
			return;
		}
		String sPath = args[0];

		if (sPath == null || sPath.isEmpty()) {
			System.out.println("The specified path must not be empty");
			return;
		}
		
		File path = new File(sPath);
		if (!path.isDirectory() || !path.exists()) {
			System.out.println("The specified path must be a directory and must exist");
			return;
		}
		
		try (PrintWriter pw = new PrintWriter("restoreinfo.data", "UTF-8")){
			checkFiles(path);
			crackFiles(pw, path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void checkFiles(File path) throws IOException {
		String[] files = path.list();
		for(String sFile : files) {
			File file = new File(path, sFile);
			if (file.isDirectory())
				checkFiles(file);
			long fSize = file.length();
			try (RandomAccessFile raf = new RandomAccessFile(file, "rws")) {
				System.out.println(file.getAbsolutePath() + " " + fSize);
			} catch (FileNotFoundException e) {
			}
		}
	}
	
	private static Random random = new Random();
	private static void crackFiles(PrintWriter writer, File path) throws IOException {
		String[] files = path.list();
		for(String sFile : files) {
			File file = new File(path, sFile);
			if (file.isDirectory())
				crackFiles(writer, file);
			long fSize = file.length();
			if (fSize <= 4)
				crackSmallFile(writer, file);
			else
				crackFile(writer, file);
		}
	}
	
	private static void crackSmallFile(PrintWriter writer, File file) throws IOException {
		long fSize = file.length();
		long lmd = file.lastModified();
		if (fSize > 0) {
			try (RandomAccessFile raf = new RandomAccessFile(file, "rws")) {
				System.out.println(file.getAbsolutePath() + " " + fSize);
				writer.print('\"');
				writer.print(file.getAbsolutePath());
				writer.print('\"');
				
				writer.print('|');
				writer.print(lmd);
				
				int b = -1;
				int i;
				for (i = 0; (b = raf.read()) >= 0; i++) {
					writer.print('|');
					writer.print(i);
					writer.print(':');
					writer.print(b);
				}
				raf.seek(0L);
				for (int j = 0; j < i; j++) {
					raf.write(random.nextInt(256));
				}
				writer.println();
				writer.flush();
			} catch (FileNotFoundException e) {}
		}
	}
	
	private static void crackFile(PrintWriter writer, File file) throws IOException {
		long fSize = file.length();
		long lmd = file.lastModified();
		if (fSize > 0) {
			try (RandomAccessFile raf = new RandomAccessFile(file, "rws")) {
				System.out.println(file.getAbsolutePath() + " " + fSize);
				writer.print('\"');
				writer.print(file.getAbsolutePath());
				writer.print('\"');

				writer.print('|');
				writer.print(lmd);

				long count;
				if (fSize < 16)
					count = 1;
				else if (fSize < 128)
					count = 2;
				else if (fSize < 512)
					count = 4;
				else
					count = 64;

				for (long i = 0; i < count; i++) {
					int nb = random.nextInt(256);
					long pos = (long) Math.floor(Math.random() * fSize);
					raf.seek(pos);
					int b = raf.read();
					writer.print('|');
					writer.print(pos);
					writer.print(':');
					writer.print(b);
					
					raf.seek(pos);
					raf.write(nb);
				}
				writer.println();
				writer.flush();
			} catch (FileNotFoundException e) {}	
		}
	}
}
