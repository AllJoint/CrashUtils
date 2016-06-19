package ru.alljoint.crashutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class FilesToEmailSender {
	private static Properties properties = System.getProperties();
	private static String filesCharset = Charset.forName("UTF-8").name();
	private static Random r = new Random();

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("ru.alljoint.crashutils.FilesToEmailSender <work dir> [<text files charset>]");
			System.exit(1);
		}
		String wrkPath = args[0];
		if (args.length > 1) {
			filesCharset = args[1];
			if (!Charset.isSupported(filesCharset)) {
				System.err.println(String.format("Chraset %s not supported", filesCharset));
				System.exit(2);
			}
		}
		
		File workDir = new File(wrkPath);
		if (!workDir.exists() || !workDir.isDirectory()) {
			System.out.println(String.format("Work directory \"%s\" must be exist", workDir.getAbsolutePath()));
			System.exit(3);
		}
		
		try {
			properties.load(new InputStreamReader(new FileInputStream("spamassassin.properties"), "UTF-8"));

			Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication(properties.getProperty("userName"),
	                		properties.getProperty("password"));
	          }
	        });
			
			File[] files = workDir.listFiles();
			computeTextFiles(files, session);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void computeTextFiles(File[] files, Session session) throws IOException, MessagingException {
		for (File file : files) {
			if (file.isDirectory())
				computeTextFiles(file.listFiles(), session);
			else if (file.getName().endsWith(".txt")) {
				StringBuilder body = new StringBuilder(8192);
				String subject = file.getName().replace(".txt", "");
				try (FileInputStream fis = new FileInputStream(file);
						InputStreamReader isr = new InputStreamReader(fis, filesCharset);
						BufferedReader br = new BufferedReader(isr)) {
					String line;
					while((line = br.readLine()) != null) {
						body.append(line);
						body.append('\n');
					}
				}
				
				String sBody = body.toString();
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

					System.out.println(String.format("Message \"%s\"\n %d messages left. Sent message successfully....", subject, i));
					if (i > 0) {
						try {
							Thread.sleep(delay);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
	}
}
