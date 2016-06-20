package ru.alljoint.crashutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.zip.ZipInputStream;

public class Dictionary {
	private static char[] spec_chars = new char[] {0x02e6, 0x05d0, 0x060f, 0x0677, 0x07da, 0x0804, 0x0827,
			0x08a2, 0x0913, 0x0c88, 0x0f53, 0x1177, 0x1256, 0x15d6, 0x17a6, 0x1a16, 0x203b, 0x26C6, 0x26D3,
			0x26E3, 0x26D0, 0x26BC, 0x26B7, 0x269F, 0x268E, 0xdf81, 0xdf81, 0xdf81, 0xdf82, 0xdf83, 0xdf84,
			0xdf85, 0xdf86, 0xdf87, 0xdf88, 0xdf89, 0xdf8a, 0xdf8b, 0xdf8c, 0xdf8d, 0xdf8e, 0xdf8f, 0xdf90};

	private static ArrayList<String> ruDict = new ArrayList<>(952000);
	private static ArrayList<String> enDict = new ArrayList<>(159000);
	private static Random random = new Random();

	public Dictionary(File ruDictFile, String ruDictCharset, File enDictFile, String enDictCharset) throws IOException {
		if (ruDict.isEmpty()) {
			readDictionary(openDictStream(ruDictFile), ruDictCharset, ruDict);
			readDictionary(openDictStream(enDictFile), enDictCharset, enDict);
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
	
	private static InputStream openDictStream(File dictFile) throws IOException {
		InputStream is = new FileInputStream(dictFile);
		if (dictFile.getName().endsWith(".zip")) {
			ZipInputStream zis = new ZipInputStream(is);
			zis.getNextEntry();
			return zis;
		}
		return is;
	}
	
	public ArrayList<String> getRussian() {
		return ruDict;
	}
	
	public String getRandomRussianWord() {
		return ruDict.get(random.nextInt(ruDict.size()));
	}
	
	public String getRandomRuCrashWord() {
		return crashWord(getRandomRussianWord());
	}
	
	public Iterator<String> russianIterator() {
		return new DictionaryIterator(ruDict);
	}
	
	public Iterator<String> russianCrashIterator() {
		return new DictionaryCrashIterator(ruDict);
	}
	
	private String crashWord(String word) {
		StringBuilder sb = new StringBuilder(word);
		sb.insert(random.nextInt(word.length()+1), spec_chars[random.nextInt(spec_chars.length)]);
		if (word.length() > 2) {
			sb.insert(random.nextInt(word.length()+1), spec_chars[random.nextInt(spec_chars.length)]);
		}
		return sb.toString();
	}
	
	public ArrayList<String> getEnglish() {
		return enDict;
	}
	
	public String getRandomEnglishWord() {
		return enDict.get(random.nextInt(enDict.size()));
	}
	
	public String getRandomEnCrashWord() {
		return crashWord(getRandomEnglishWord());
	}
	
	public Iterator<String> englishIterator() {
		return new DictionaryIterator(enDict);
	}
	
	public Iterator<String> englishCrashIterator() {
		return new DictionaryCrashIterator(enDict);
	}

	private class DictionaryIterator implements Iterator<String> {
		private int index = 0;
		private int size;
		private ArrayList<String> dict;
		
		public DictionaryIterator(ArrayList<String> dict) {
			this.dict = dict;
			this.size = dict.size();
		}

		@Override
		public boolean hasNext() {
			return index < (size - 1);
		}

		@Override
		public String next() {
			return dict.get(index++);
		}
	}
	
	private class DictionaryCrashIterator implements Iterator<String> {
		private int index = 0;
		private int size;
		private ArrayList<String> dict;
		
		public DictionaryCrashIterator(ArrayList<String> dict) {
			this.dict = dict;
			this.size = dict.size();
		}

		@Override
		public boolean hasNext() {
			return index < (size - 1);
		}

		@Override
		public String next() {
			return crashWord(dict.get(index++));
		}
	}
	
}
