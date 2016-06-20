package ru.alljoint.crashutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class BullshitEmailSender {
	private static Properties properties = System.getProperties();
	private static Random r = new Random();

	public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println("ru.alljoint.crashutils.BullshitEmailSender <russian dict path[:charset]>"
					+ " <english dict path[:charset]> <crashed words> <message count>\n");
			System.out.println("Example: ru.alljoint.crashutils.BullshitEmailSender ru.dicttionary:KOI8-R en.dictionary.zip true 100");
			System.exit(1);
		}
		
		String ruDictPath = args[0];
		String ruDictCharset = Charset.defaultCharset().name();
		String enDictPath = args[1];
		String enDictCharset = Charset.defaultCharset().name();
		boolean crashed = Boolean.parseBoolean(args[2]);
		int messageCount = Integer.parseInt(args[3]);

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
			
			for (int mc = 0; mc < messageCount; mc++) {
				String sBody = generateBody(dict, crashed);
				String subject = dict.getRandomRussianWord();
				int i = Integer.parseInt(properties.getProperty("message.count")) + r.nextInt(2);
				int delay = Integer.parseInt(properties.getProperty("message.delay"));
				for (; i > 0; i--) {
					{
						MimeMessage message = new MimeMessage(session);
						message.setFrom(new InternetAddress(properties.getProperty("message.from")));
						message.addRecipient(Message.RecipientType.TO,
								new InternetAddress(properties.getProperty("message.to")));
						message.setSubject(subject);
						message.setText(sBody);

						Transport.send(message);
					}

					System.out.println(String.format("Message \"%s\"\n %d messages left. Sent message successfully....",
							subject, i));
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

	private static String generateBody(Dictionary dict, boolean crashed) {
		int wcount = 1000 + r.nextInt(2000);
		StringBuilder sb = new StringBuilder(wcount*10);
		for (int i = 0; i < wcount; i++) {
			if (r.nextInt(100) < 5)
				sb.append(crashed ? dict.getRandomEnCrashWord() : dict.getRandomEnglishWord());
			else
				sb.append(crashed ? dict.getRandomRuCrashWord() : dict.getRandomRussianWord());
			
			if (r.nextInt(100) < 5)
				sb.append('\n');
			else
				sb.append(' ');
		}
		return sb.toString();
	}
}
