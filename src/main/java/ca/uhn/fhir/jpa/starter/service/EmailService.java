package ca.uhn.fhir.jpa.starter.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;

@Service
public class EmailService {
	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
	private final JavaMailSender javaMailSender;

	@Value("${spring.mail.username}")
	private String emailSender;


	public EmailService(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	@RabbitListener(queues = "${rabbitmq.queue.email.name}")
	public void sendEmailRabbit(EmailDetails emailDetails) {
		try {
			if(emailDetails.getAttachment() != null && emailDetails.getAttachmentName() != null){
				MimeMessage mimeMessage = javaMailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

				helper.setFrom(emailSender);
				helper.setTo(emailDetails.getRecipient());
				helper.setSubject(emailDetails.getSubject());
				helper.setText(emailDetails.getMessageBody());

				helper.addAttachment(emailDetails.getAttachmentName(),
					() -> new ByteArrayInputStream(emailDetails.getAttachment()));

				javaMailSender.send(mimeMessage);
				logger.info("Mail with attachment sent successfully to {}", emailDetails.getRecipient());
			} else {
				SimpleMailMessage mailMsg = new SimpleMailMessage();
				mailMsg.setFrom(emailSender);
				mailMsg.setTo(emailDetails.getRecipient());
				mailMsg.setText(emailDetails.getMessageBody());
				mailMsg.setSubject(emailDetails.getSubject());

				javaMailSender.send(mailMsg);
				logger.info("Mail sent successfully  to {}", emailDetails.getRecipient());
			}
		} catch (MailException | MessagingException e) {
			logger.error("Sending mail failed to {} due to: {}", emailDetails.getRecipient(), e.getMessage(), e);
		}
	}

	@Setter
	@Getter
	public static class EmailDetails {
		private String recipient;
		private String messageBody;
		private String subject;
		private byte[] attachment;
		private String attachmentName;

		public EmailDetails() {}

		public EmailDetails(String recipient, String messageBody, String subject, byte[] attachment, String attachmentName) {
			this.recipient = recipient;
			this.messageBody = messageBody;
			this.subject = subject;
			this.attachment = attachment;
			this.attachmentName = attachmentName;
		}
	}
}