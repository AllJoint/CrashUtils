package ru.alljoint.crashutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class DictEmailSender {
	private static Properties properties = System.getProperties();
	private static Random r = new Random();

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("ru.alljoint.crashutils.DictEmailSender <russian dict path[:charset]>"
					+ " <english dict path[:charset]> <crashed words>\n");
			System.out.println("Example: ru.alljoint.crashutils.DictEmailSender ru.dicttionary:KOI8-R en.dictionary.zip true");
			System.exit(1);
		}
		
		String ruDictPath = args[0];
		String ruDictCharset = Charset.defaultCharset().name();
		String enDictPath = args[1];
		String enDictCharset = Charset.defaultCharset().name();
		boolean crashed = Boolean.parseBoolean(args[2]);

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
			properties.load(new InputStreamReader(new FileInputStream("spamassassin.properties"), "UTF-8"));

			Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication(properties.getProperty("userName"),
	                		properties.getProperty("password"));
	          }
	        });
			
			Dictionary dict = new Dictionary(new File(ruDictPath), ruDictCharset,
					new File(enDictPath), enDictCharset);
			
			Iterator<String> ruIter = (crashed ? dict.russianCrashIterator() : dict.russianIterator());
			int count = dict.getRussian().size();
			while (ruIter.hasNext()) {
				StringBuilder sb = new StringBuilder(20000);
				for (int i = 0; i < 2000 && ruIter.hasNext(); i++) {
					sb.append(ruIter.next());
					count--;
					
					if (r.nextInt(100) < 5)
						sb.append('\n');
					else
						sb.append(' ');
				}
				String sBody = sb.toString();
				String subject = (crashed ? (dict.getRandomRuCrashWord() + ' ' + dict.getRandomEnCrashWord()) :
					(dict.getRandomRussianWord() + ' ' + dict.getRandomEnglishWord()));
				
				int i = Integer.parseInt(properties.getProperty("message.count"));
				int delay = Integer.parseInt(properties.getProperty("message.delay"));
				for (; i > 0; i--) {
					sendMessage(subject, sBody, session);

					System.out.println(String.format("Message \"%s\" sent successfully.... %d russian words left",
							subject, count));
					if (i > 0) {
						try {
							Thread.sleep(delay);
						} catch (InterruptedException e) {
						}
					}
				}
			}
			
			Iterator<String> enIter = (crashed ? dict.englishCrashIterator() : dict.englishIterator());
			count = dict.getEnglish().size();
			while (enIter.hasNext()) {
				StringBuilder sb = new StringBuilder(20000);
				for (int i = 0; i < 2000 && enIter.hasNext(); i++) {
					sb.append(enIter.next());
					count--;
					
					if (r.nextInt(100) < 5)
						sb.append('\n');
					else
						sb.append(' ');
				}
				String sBody = sb.toString();
				String subject = dict.getRandomRuCrashWord() + ' ' + dict.getRandomEnCrashWord();
				
				int i = Integer.parseInt(properties.getProperty("message.count"));
				int delay = Integer.parseInt(properties.getProperty("message.delay"));
				for (; i > 0; i--) {
					sendMessage(subject, sBody, session);

					System.out.println(String.format("Message \"%s\" sent successfully.... %d english words left",
							subject, count));
					if (i > 0) {
						try {
							Thread.sleep(delay);
						} catch (InterruptedException e) {
						}
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void sendMessage(String subject, String body, Session session) throws MessagingException {
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(properties.getProperty("message.from")));
		message.addRecipient(Message.RecipientType.TO,
				new InternetAddress(properties.getProperty("message.to")));
		message.setSubject(subject, "utf-8");
		message.setText(body, "utf-8");

		Transport.send(message);
	}
}
