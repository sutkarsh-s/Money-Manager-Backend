package in.utkarshsingh.money.manager.email.adapter.smtp;

import in.utkarshsingh.money.manager.email.domain.EmailMessage;
import in.utkarshsingh.money.manager.email.port.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Infrastructure adapter: sends email via SMTP (implements EmailSender port).
 * DIP: High-level use cases depend on EmailSender; this is one implementation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    @Override
    public void send(EmailMessage message) {
        log.info("Sending email to {}", message.getRecipient());
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromEmail);
        mail.setTo(message.getRecipient());
        mail.setSubject(message.getSubject());
        mail.setText(message.getBody());
        mailSender.send(mail);
        log.info("Email sent successfully to: {}", message.getRecipient());
    }
}
