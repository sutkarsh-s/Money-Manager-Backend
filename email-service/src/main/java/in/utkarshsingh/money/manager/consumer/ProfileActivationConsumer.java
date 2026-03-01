package in.utkarshsingh.money.manager.consumer;

import in.utkarshsingh.money.manager.event.ProfileActivationEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileActivationConsumer {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    @Value("${app.activation.url:http://localhost:8080}")
    private String activationBaseUrl;

    @RabbitListener(queues = "profile-activation-queue")
    public void handleProfileActivation(ProfileActivationEvent event) {
        log.info("Received activation event for: {}", event.getEmail());
        try {
            String activationLink = activationBaseUrl + "/api/v1/activate?token=" + event.getActivationToken();

            Context ctx = new Context();
            ctx.setVariable("fullName", event.getFullName());
            ctx.setVariable("activationLink", activationLink);
            String htmlBody = templateEngine.process("activation-email", ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(event.getEmail());
            helper.setSubject("Activate Your Money Manager Account");
            helper.setText(htmlBody, true);
            mailSender.send(message);

            log.info("Activation email sent to: {}", event.getEmail());
        } catch (MessagingException ex) {
            log.error("Failed to send activation email to: {} | error={}", event.getEmail(), ex.getMessage());
        }
    }
}
