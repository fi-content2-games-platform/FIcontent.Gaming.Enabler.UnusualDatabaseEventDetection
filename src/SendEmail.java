/*
Sends an email using a gmail address.
Gmail may have to be configured to allow sending such emails.
 */

// mostly from stackoverflow and http://www.tutorialspoint.com/java/java_sending_email.htm

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/** Sends an email using a gmail address.

Gmail may have to be configured to allow sending such emails.
 */

public class SendEmail {
	private SendEmail() {
	}

	public static void send(final String sender, final String password, String recipient, String subject, String message) throws RuntimeException {
		Properties props = new Properties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(sender, password); // here, username equals sender
			}
		  });

		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(sender));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
			msg.setSubject(subject);
			msg.setText(message);

			if (Uded.DEBUG) System.out.println("trying to send email...");
			Transport.send(msg);
			if (Uded.DEBUG) System.out.println("done sending email");
		} catch (MessagingException e) {
			if (Uded.DEBUG) System.out.println("error sending email");
			throw new RuntimeException(e);
		}
	}
}
