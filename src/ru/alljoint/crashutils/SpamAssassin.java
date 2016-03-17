package ru.alljoint.crashutils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SpamAssassin {

	public static void main(String[] args) {
		try {
			Properties properties = System.getProperties();
			properties.load(new InputStreamReader(new FileInputStream("spamassassin.properties"), "UTF-8"));
			Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication("akurgan", "20NfyfqcrfzCrfprf");
	          }
	        });

			int i = Integer.parseInt(properties.getProperty("message.count"));
			for (; i > 0; i--) {
				{
					MimeMessage message = new MimeMessage(session);
					message.setFrom(new InternetAddress(properties.getProperty("message.from")));
					message.addRecipient(Message.RecipientType.TO,
							new InternetAddress(properties.getProperty("message.to")));
					message.setSubject(properties.getProperty("message.subject"));
					message.setText(properties.getProperty("message.text"));
					
					Transport.send(message);
				}

				System.out.println(i + " messages left. Sent message successfully....");
				if (i != 1) {
					try {
						Thread.sleep(1000 * 60 * 15);
					} catch (InterruptedException e) {
					}
				}
			}
		} catch (Exception mex) {
			mex.printStackTrace();
		}
	}
}
