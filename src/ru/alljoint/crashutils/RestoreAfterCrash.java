package ru.alljoint.crashutils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class RestoreAfterCrash {

	private static class Part {
		public long pos;
		public int b;
		
		public Part(long pos, int b) {
			this.pos = pos;
			this.b = b;
		}
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("File information for recovery should be specified");
			return;
		}
		String sResFile = args[0];
		
		try (FileInputStream fis = new FileInputStream(sResFile);
				InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
				BufferedReader br = new BufferedReader(isr, 32768)) {
			String line;
			while((line = br.readLine()) != null) {
				String[] parts = line.split("\\|");
				String fileName = parts[0].substring(1, parts[0].length()-1);
				long lmd = Long.parseLong(parts[1]);
				File file = new File(fileName);
				
				List<Part> pl = new ArrayList<Part>(parts.length-1);
				for (int i = 2; i < parts.length; i++) {
					String[] nps = parts[i].split("\\:");
					pl.add(new Part(Long.parseLong(nps[0]), Integer.parseInt(nps[1])));
				}
				try (RandomAccessFile raf = new RandomAccessFile(file, "rws")) {
					for (int i = pl.size()-1; i >= 0; i--) {
						Part p = pl.get(i);
						raf.seek(p.pos);
						raf.write(p.b);
					}
				}
				
				file.setLastModified(lmd);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
