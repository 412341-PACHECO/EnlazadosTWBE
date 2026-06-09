package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.exceptions.EmailDeliveryException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;
	private final String fromEmail;
	private final String verifyEmailUrl;
	private final String resetPasswordUrl;

	public EmailService(
		JavaMailSender mailSender,
		@Value("${app.email.from}") String fromEmail,
		@Value("${app.frontend.verify-email-url}") String verifyEmailUrl,
		@Value("${app.frontend.reset-password-url}") String resetPasswordUrl
	) {
		this.mailSender = mailSender;
		this.fromEmail = fromEmail;
		this.verifyEmailUrl = verifyEmailUrl;
		this.resetPasswordUrl = resetPasswordUrl;
	}

	public void sendVerificationEmail(User user, String token) {
		String verificationLink = verifyEmailUrl + "?token=" + token;
		String subject = "Confirma tu cuenta en EnlazadosTW";
		String html = """
			<h2>Confirma tu cuenta</h2>
			<p>Hola %s,</p>
			<p>Para activar tu cuenta en EnlazadosTW, confirma tu email desde el siguiente enlace:</p>
			<p><a href="%s">Confirmar cuenta</a></p>
			<p>Si no solicitaste este registro, ignora este mensaje.</p>
			""".formatted(user.getFirstName(), verificationLink);

		sendHtmlEmail(user.getEmail(), subject, html);
	}

	public void sendPasswordResetEmail(User user, String token) {
		String resetLink = resetPasswordUrl + "?token=" + token;
		String subject = "Restablece tu contrasena en EnlazadosTW";
		String html = """
			<h2>Restablecimiento de contrasena</h2>
			<p>Hola %s,</p>
			<p>Recibimos una solicitud para cambiar tu contrasena.</p>
			<p><a href="%s">Cambiar contrasena</a></p>
			<p>Si no solicitaste este cambio, ignora este mensaje.</p>
			""".formatted(user.getFirstName(), resetLink);

		sendHtmlEmail(user.getEmail(), subject, html);
	}

	private void sendHtmlEmail(String to, String subject, String html) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(html, true);
			mailSender.send(mimeMessage);
		} catch (MessagingException | MailException ex) {
			logger.error("No se pudo enviar email a {}", to, ex);
			throw new EmailDeliveryException("No se pudo enviar el correo electronico", ex);
		}
	}
}
